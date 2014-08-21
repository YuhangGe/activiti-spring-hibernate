package me.xiaoge.web;

import me.xiaoge.model.User;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by xiaoge on 2014/8/21.
 */
@Controller
@RequestMapping("/acti")
public class ActiController {
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;

    @RequestMapping(method = RequestMethod.GET)
    public String index(Model model) {

        runtimeService.startProcessInstanceByKey("myProcess");

        List<Task> taskList = taskService.createTaskQuery().list();

        User us = new User();
        us.setName("xiaoge");
        model.addAttribute("user", us);
        model.addAttribute("taskList", taskList);

        return "acti/index";
    }
}
