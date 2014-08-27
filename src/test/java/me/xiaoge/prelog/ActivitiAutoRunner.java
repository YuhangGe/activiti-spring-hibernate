package me.xiaoge.prelog;

import me.xiaoge.prelog.autorun.RhoExpressionCondition;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static junit.framework.Assert.assertEquals;

/**
 * Created by abraham on 14/8/27.
 */
public class ActivitiAutoRunner extends AbstractPreLogTest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void test() {
        String testProcessDefinitionKeyName = "autoTaskProcess";

        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().processDefinitionKey(testProcessDefinitionKeyName).list();

        assertEquals(processDefinitionList.size(), 1);

        ProcessDefinition processDefinition = processDefinitionList.get(0);

        ProcessDefinitionEntity pd = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processDefinition.getId());

        List<ActivityImpl> activityList = pd.getActivities();

        print(activityList.size());

        for(ActivityImpl ai : activityList) {
            Map<String, Object> proMap = ai.getProperties();
            if(proMap == null || !proMap.containsKey("type")) {
                continue;
            }
            String type = (String)proMap.get("type");
            if(type.equals("exclusiveGateway") || type.equals("")) {
                List<PvmTransition> outgoingTransitions = ai.getOutgoingTransitions();
                int i = 0;
                for (PvmTransition outgoingTransition : outgoingTransitions) {
                    TransitionImpl ti = (TransitionImpl) outgoingTransition;
                    Map<String, Object> tip = ti.getProperties();
                    if(tip == null || !tip.containsKey("condition")) {
                        continue;
                    }
                    Object ct = tip.get("condition");
                    if(ct == null ||  !(ct instanceof UelExpressionCondition)) {
                        continue;
                    }
                    RhoExpressionCondition rec = new RhoExpressionCondition(i==1);
                    print(rec.getValue());
                    tip.put("condition", rec);
                    i++;

                }
            }
        }


        Random random = new Random();

//        for (int i = 0; i < 5; i++) {
            int c = random.nextInt(2) + 1;
            HashMap<String, Object> varMap = new HashMap<>();
            varMap.put("chooice", c);
            runtimeService.startProcessInstanceByKey("autoTaskProcess", varMap);
//        }

    }

}
