package me.xiaoge.prelog;

import me.xiaoge.prelog.autorun.RhoExpressionCondition;
import me.xiaoge.prelog.autorun.RhoExpressionExclusiveHolder;
import me.xiaoge.prelog.autorun.RhoExpressionHolder;
import me.xiaoge.prelog.autorun.RhoExpressionManager;
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
    public void test() throws Exception {
        String testProcessDefinitionKeyName = "autoTaskProcess";

        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().processDefinitionKey(testProcessDefinitionKeyName).list();

        assertEquals(processDefinitionList.size(), 1);

        ProcessDefinition processDefinition = processDefinitionList.get(0);

        ProcessDefinitionEntity pd = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processDefinition.getId());

        List<ActivityImpl> activityList = pd.getActivities();

        print(activityList.size());

        RhoExpressionManager expressionManager = new RhoExpressionManager();

        for(ActivityImpl ai : activityList) {
            Map<String, Object> proMap = ai.getProperties();
            if(proMap == null || !proMap.containsKey("type")) {
                continue;
            }
            String type = (String)proMap.get("type");
            if(type.equals("inclusiveGateway")) {
                throw new Exception("inclusive gateway is not support!");
            } if(type.equals("exclusiveGateway")) {
                List<PvmTransition> outgoingTransitions = ai.getOutgoingTransitions();
                RhoExpressionHolder expressionHolder = new RhoExpressionExclusiveHolder();
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
                    RhoExpressionCondition rec = new RhoExpressionCondition();
                    expressionHolder.addCondition(rec);
                }

                expressionManager.addExpressionHolder(expressionHolder);

            }
        }

        /**
         * debugMax和debugIdx是为了防止陷入死循环。
         */
        int debugMax = 10000;
        int debugIdx = 0;
        while(debugIdx < debugMax && !expressionManager.isFinish()) {
            expressionManager.run();
            runtimeService.startProcessInstanceByKey(testProcessDefinitionKeyName);
            debugIdx++;
        }

        if(debugIdx == debugMax) {
            throw new Exception("expression manager go into infinite loop");
        }
    }

}
