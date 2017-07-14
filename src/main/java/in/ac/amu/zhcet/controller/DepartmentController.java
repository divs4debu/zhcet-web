package in.ac.amu.zhcet.controller;

import in.ac.amu.zhcet.data.model.Course;
import in.ac.amu.zhcet.data.model.FacultyMember;
import in.ac.amu.zhcet.data.service.DepartmentAdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DepartmentController {

    private final DepartmentAdminService departmentAdminService;

    public DepartmentController(DepartmentAdminService departmentAdminService) {
        this.departmentAdminService = departmentAdminService;
    }

    @GetMapping("/department")
    public String department(Model model) {
        Course course = new Course();
        model.addAttribute("newCourse", course);
        model.addAttribute("floatedCourses", departmentAdminService.getFloatedCourses());
        model.addAttribute("courses", departmentAdminService.getAllCourses());
        FacultyMember facultyMember = departmentAdminService.getFacultyMember();
        model.addAttribute("department", facultyMember.getDepartment());

        // TODO: Show floated courses and option to float course by selecting existing course

        return "department";
    }

    @PostMapping("/department/create_course")
    public String createCourse(@ModelAttribute Course course) {
        // TODO: Add form handling
        departmentAdminService.registerCourse(course);

        return "redirect:/department";
    }

    @PostMapping("/department/float_course")
    public String floatCourse(@ModelAttribute Course course) {
        // TODO: Add form handling
        try {
            departmentAdminService.floatCourse(course);
        } catch (IllegalAccessException exc) {
            // TODO: Add flash message exc.getMessage
        }

        return "redirect:/department";
    }

}
