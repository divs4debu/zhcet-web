package amu.zhcet.core.department.floated.create;

import amu.zhcet.common.page.Path;
import amu.zhcet.common.page.PathChain;
import amu.zhcet.core.department.course.CoursesController;
import amu.zhcet.core.error.ErrorUtils;
import amu.zhcet.data.course.Course;
import amu.zhcet.data.course.floated.FloatedCourseService;
import amu.zhcet.data.department.Department;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/department/{department}/course/float")
public class FloatCourseController {

    private final FloatedCourseService floatedCourseService;

    @Autowired
    public FloatCourseController(FloatedCourseService floatedCourseService) {
        this.floatedCourseService = floatedCourseService;
    }

    public static PathChain getPath(Department department) {
        return CoursesController.getPath(department)
                .add(Path.builder().title("Float")
                        .link(String.format("/department/%s/course/float", department.getCode()))
                        .build());
    }

    @GetMapping("/{course}")
    public String floatCourse(@PathVariable Department department, @PathVariable Course course, RedirectAttributes redirectAttributes) {
        ErrorUtils.requireNonNullDepartment(department);
        ErrorUtils.requireNonNullCourse(course);

        if (floatedCourseService.isFloated(course)) {
            log.warn("Course is already floated {}", course.getCode());
            redirectAttributes.addFlashAttribute("float_error", "Course is already floated");
        }  else {
            redirectAttributes.addFlashAttribute("courses", Collections.singletonList(course));
        }

        return "redirect:/department/{department}/course/float";
    }

    @GetMapping
    public String floatCourse(Model model, @PathVariable Department department) {
        ErrorUtils.requireNonNullDepartment(department);

        model.addAttribute("page_title", "Float Course : " + department.getName() + " Department");
        model.addAttribute("page_subtitle", "Floated Course Management");
        model.addAttribute("page_description", "Float and manage course and faculty in-charge for this session");
        model.addAttribute("department", department);
        model.addAttribute("page_path", getPath(department));

        return "department/float_course";
    }

    @PostMapping
    public String floatCourses(RedirectAttributes redirectAttributes, @PathVariable Department department, @RequestParam("code") List<Course> courseList) {
        ErrorUtils.requireNonNullDepartment(department);

        for (Course course : courseList)
            floatedCourseService.floatCourse(course);

        redirectAttributes.addFlashAttribute("float_success", "Courses floated successfully!");

        if (courseList.size() == 1)
            return String.format("redirect:/department/{department}/floated/%s", courseList.get(0).getCode());

        return "redirect:/department/{department}/course/float";
    }

}