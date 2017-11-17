package in.ac.amu.zhcet.service.upload.csv;

import com.j256.simplecsv.processor.CsvProcessor;
import in.ac.amu.zhcet.data.model.Department;
import in.ac.amu.zhcet.data.model.FacultyMember;
import in.ac.amu.zhcet.data.model.dto.upload.FacultyUpload;
import in.ac.amu.zhcet.data.repository.DepartmentRepository;
import in.ac.amu.zhcet.service.FacultyService;
import in.ac.amu.zhcet.service.upload.csv.base.AbstractUploadService;
import in.ac.amu.zhcet.service.upload.csv.base.Confirmation;
import in.ac.amu.zhcet.service.upload.csv.base.UploadResult;
import in.ac.amu.zhcet.service.upload.storage.FileSystemStorageService;
import in.ac.amu.zhcet.service.upload.storage.FileType;
import in.ac.amu.zhcet.utils.SecurityUtils;
import in.ac.amu.zhcet.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FacultyUploadService {
    private boolean invalidDepartment;
    private boolean duplicateFacultyId;

    private final DepartmentRepository departmentRepository;
    private final FacultyService facultyService;
    private final AbstractUploadService<FacultyUpload, FacultyMember> uploadService;
    private final FileSystemStorageService systemStorageService;
    private final static int PASS_LENGTH = 12;

    @Autowired
    public FacultyUploadService(
            DepartmentRepository departmentRepository,
            FacultyService facultyService,
            AbstractUploadService<FacultyUpload, FacultyMember> uploadService,
            FileSystemStorageService fileSystemStorageService
    ) {
        this.departmentRepository = departmentRepository;
        this.facultyService = facultyService;
        this.uploadService = uploadService;
        this.systemStorageService = fileSystemStorageService;
    }

    public UploadResult<FacultyUpload> handleUpload(MultipartFile file) throws IOException {
        return uploadService.handleUpload(FacultyUpload.class, file);
    }

    private String getMappedValue(FacultyMember facultyMember, List<Department> departments) {
        String departmentName = facultyMember.getUser().getDepartment().getName();

        Optional<Department> optional = departments.stream()
                .filter(department -> department.getName().equals(departmentName))
                .findFirst();

        if (!optional.isPresent()) {
            invalidDepartment = true;
            log.info("Faculty Registration : Invalid Department {}", departmentName);
            return "No such department: " + departmentName;
        } else if (facultyService.getById(facultyMember.getFacultyId()) != null) {
            duplicateFacultyId = true;
            log.info("Duplicate Faculty ID {}", facultyMember.getFacultyId());
            return "Duplicate Faculty ID";
        } else {
            facultyMember.getUser().setDepartment(optional.get());
            return null;
        }
    }

    public Confirmation<FacultyMember> confirmUpload(UploadResult<FacultyUpload> uploadResult) {
        invalidDepartment = false;
        duplicateFacultyId = false;

        List<Department> departments = departmentRepository.findAll();

        Confirmation<FacultyMember> facultyConfirmation = uploadService.confirmUpload(
                uploadResult,
                FacultyUploadService::fromFacultyUpload,
                facultyMember -> getMappedValue(facultyMember, departments)
        );

        if (invalidDepartment)
            facultyConfirmation.getErrors().add("Faculty Member with invalid department found");
        if (duplicateFacultyId)
            facultyConfirmation.getErrors().add("Faculty Member with duplicate Faculty ID found");

        if (!facultyConfirmation.getErrors().isEmpty()) {
            log.warn(facultyConfirmation.getErrors().toString());
        }

        return facultyConfirmation;
    }

    public String registerFaculty(Confirmation<FacultyMember> confirmation) throws IOException {
        String filename = saveFile(confirmation);
        long started = System.currentTimeMillis();
        facultyService.register(confirmation.getData());
        log.warn("Saved {} faculty members in {} ms", confirmation.getData().size(), System.currentTimeMillis() - started);

        return filename;
    }

    private String saveFile(Confirmation<FacultyMember> confirmation) throws IOException {
        String filename = systemStorageService.generateFileName("faculty_password.csv");

        Path filePath = systemStorageService.load(FileType.CSV, filename);
        File newFile = filePath.toFile();

        List<FacultyUpload> facultyUploads = confirmation.getData().stream()
                .map(FacultyUploadService::fromFacultyMember)
                .collect(Collectors.toList());

        CsvProcessor<FacultyUpload> csvProcessor = new CsvProcessor<>(FacultyUpload.class);
        csvProcessor.writeAll(newFile, facultyUploads, true);

        return filename;
    }

    private static FacultyMember fromFacultyUpload(FacultyUpload facultyUpload) {
        String password = SecurityUtils.generatePassword(PASS_LENGTH);
        facultyUpload.setPassword(password);
        FacultyMember facultyMember = new FacultyMember();
        facultyMember.setFacultyId(StringUtils.capitalizeAll(facultyUpload.getFacultyId()));
        facultyMember.setDesignation(StringUtils.capitalizeFirst(facultyUpload.getDesignation()));
        facultyMember.getUser().setName(StringUtils.capitalizeFirst(facultyUpload.getName()));
        facultyMember.getUser().setPassword(password);
        facultyMember.getUser().setDepartment(Department.builder().name(StringUtils.capitalizeFirst(facultyUpload.getDepartment())).build());
        facultyMember.getUser().getDetails().setGender(facultyUpload.getGender());

        return facultyMember;
    }

    private static FacultyUpload fromFacultyMember(FacultyMember facultyMember) {
        FacultyUpload facultyUpload = new FacultyUpload();
        facultyUpload.setFacultyId(facultyMember.getFacultyId());
        facultyUpload.setName(facultyMember.getUser().getName());
        facultyUpload.setDesignation(facultyMember.getDesignation());
        facultyUpload.setDepartment(facultyMember.getUser().getDepartment().getName());
        facultyUpload.setPassword(facultyMember.getUser().getPassword());

        return facultyUpload;
    }
}
