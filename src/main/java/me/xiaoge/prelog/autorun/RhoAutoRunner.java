package me.xiaoge.prelog.autorun;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.ProcessDefinition;

import java.util.List;
import java.util.Map;

/**
 * Created by abraham on 14/9/3.
 */
public class RhoAutoRunner {

    private RepositoryService repositoryService;

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public RhoAutoRunner() {

    }

    public RhoAutoRunner(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void runProcessByDefinitionKeyName(RuntimeService runtimeService, String processDefinitionKeyName) throws Exception {
        if(this.repositoryService == null) {
            throw new Exception("RhoAutoRunner: please init repositoryService first!");
        }

        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKeyName).list();

        if(processDefinitionList.size()==0) {
            throw new Exception("RhoAutoRunner: no process definition found!");
        }

        ProcessDefinition processDefinition = processDefinitionList.get(0);

        ProcessDefinitionEntity pd = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processDefinition.getId());

        List<ActivityImpl> activityList = pd.getActivities();

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
                    tip.put("condition", rec);
                }

                expressionManager.addExpressionHolder(expressionHolder);

            } else if(type.equals("parallelGateway")) {
                System.out.println("parallelGateway to do .");
                //todo 当前对于并发，还需要额外处理，使满足rho-complete的条件。non-swf.bpmn这个文件就不能被正确挖掘。
            }
        }

        /**
         * debugMax和debugIdx是为了防止陷入死循环。
         */
        int debugMax = 10000;
        int debugIdx = 0;
        while(debugIdx < debugMax && !expressionManager.isFinish()) {
            expressionManager.run();
//            expressionManager.printTo(System.out);
            runtimeService.startProcessInstanceByKey(processDefinitionKeyName);
            debugIdx++;
        }

        if(debugIdx == debugMax) {
            throw new Exception("RhoAutoRunner: expression manager go into infinite loop");
        }
    }
}
