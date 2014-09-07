package me.xiaoge.prelog;

import me.xiaoge.prelog.autorun.*;
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

    @Test
    public void test() throws Exception {
        String testProcessDefinitionKeyName = "fourParallelProcess";

        RhoAutoRunner autoRunner = new RhoAutoRunner(rhoEventLoggerBean);

        autoRunner.runProcessByDefinitionKeyName(runtimeService, testProcessDefinitionKeyName);
    }

}
