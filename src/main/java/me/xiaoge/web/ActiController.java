package me.xiaoge.web;

import me.xiaoge.model.User;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by xiaoge on 2014/8/21.
 */
@Controller
@RequestMapping("/act")
public class ActiController {
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;

    private final String processDefKey = "miniProcess";

    @RequestMapping(method = RequestMethod.GET, value = "/{username}")
    public String index(@PathVariable String username, Model model) {
        if(username.equals("a")) {
            List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery().list();
            model.addAttribute("processList", processInstanceList);
            return "act/a";
        } else if(username.equals("b")) {
            List<Task> taskList = taskService.createTaskQuery().taskName("B").processDefinitionKey(processDefKey).list();
            model.addAttribute("taskList", taskList);
            return "act/b";
        } else if(username.equals("c")) {
            List<Task> taskList = taskService.createTaskQuery().taskName("C").processDefinitionKey(processDefKey).list();
            model.addAttribute("taskList", taskList);
            return "act/c";
        }
        return "act/index";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/a")
    public String dealA(@Valid ChoiceForm cf, BindingResult result) {
        if(!result.hasErrors()) {
            if(cf.getChoice().equals("b")) {
                startProcess(true);
            } else if(cf.getChoice().equals("c")) {
                startProcess(false);
            }
        }
        return "redirect:/act/a";
    }

    private void startProcess(boolean c) {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("c", c);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miniProcess");

        Task task = taskService.createTaskQuery().taskName("A").processInstanceId(processInstance.getProcessInstanceId()).singleResult();

        taskService.complete(task.getId(), varMap);

    }

    @RequestMapping(method = RequestMethod.POST, value = "/b")
    public String dealB(String tid) {
        taskService.complete(tid);
        return "redirect:/act/b";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/c")
    public String dealC(String tid) {
        taskService.complete(tid);

        return "redirect:/act/c";
    }
}
