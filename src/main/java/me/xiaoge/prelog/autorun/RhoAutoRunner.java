package me.xiaoge.prelog.autorun;

import me.xiaoge.prelog.RhoEventListener;
import me.xiaoge.prelog.RhoEventLogEntity;
import me.xiaoge.prelog.RhoEventLogger;
import me.xiaoge.prelog.RhoEventLoggerBean;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import sun.misc.Sort;
import sun.org.mozilla.javascript.internal.Function;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by abraham on 14/9/3.
 */
public class RhoAutoRunner implements RhoEventListener {

    RhoEventLoggerBean rhoEventLoggerBean;

    private RepositoryService repositoryService;

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public RhoAutoRunner(RhoEventLoggerBean rhoEventLoggerBean) {
        this.rhoEventLoggerBean = rhoEventLoggerBean;
        this.rhoEventLoggerBean.addRhoListener(RhoEventLogger.BEFORE_STORE_LOG, this);
        this.repositoryService = rhoEventLoggerBean.getRepositoryService();
    }

    private Map<String, ParallelE> parallelCache = new HashMap<>();

    private void dealParallelGateway(ActivityImpl ai) throws Exception {
        List<PvmTransition> incomingTransitions = ai.getIncomingTransitions();
        List<PvmTransition> outgoingTransitions = ai.getOutgoingTransitions();
        /*
         * 不支持多进多出的并发gateway。
         * 如果是多进一出，说明是并发的结束，直接返回。
         * 如果是一进多出，说明是并发的开始，需要处理。
         */
        int is = incomingTransitions.size();
        int os = outgoingTransitions.size();
        if (is <= 0 || os <= 0) {
            return;
        } else if (is > 1 && os > 1) {
            throw new Exception("RhoAutoRunner: do not support such kind of parallel gateway");
        } else if (os == 1) {
            return;
        }

        PvmActivity curAct = incomingTransitions.get(0).getSource();
        String type = (String) curAct.getProperty("type");
        List<String> curTaskList = new ArrayList<>();

        if (type.endsWith("Task")) {
            curTaskList.add((String) curAct.getProperty("name"));
        } else if (type.equals("exclusiveGateway")) {
            List<PvmTransition> ins = curAct.getIncomingTransitions();
            for (PvmTransition pi : ins) {
                PvmActivity pt = pi.getSource();
                String ptype = (String) pt.getProperty("type");
                if (ptype.endsWith("Task")) {
                    curTaskList.add((String) pt.getProperty("name"));
                } else {
                    throw new Exception("RhoAutoRunner: do not support such kind of parallel gateway");
                }
            }
        } else {
            throw new Exception("RhoAutoRunner: do not support such kind of parallel gateway, with zero pre tasks");
        }
        ParallelE pairList = new ParallelE();
        for (String curTaskName : curTaskList) {
            parallelCache.put(curTaskName, pairList);
        }


        List<PvmActivity> outActList = new ArrayList<>();
        for (PvmTransition outTransition : outgoingTransitions) {
            outActList.add(outTransition.getDestination());
        }

        addOutPair(pairList.list, outActList, 0);

    }

    private void addOutPair(List<String[]> pairList, List<PvmActivity> outActList, int deep) {
        int end = outActList.size() - 1;
        if (deep >= end) {
            return;
        }
        PvmActivity curAct = outActList.get(deep);
        PvmActivity nextAct;
        for (int i = deep + 1; i <= end; i++) {
            nextAct = outActList.get(i);
            addEachOutPair(pairList, curAct, nextAct);
        }
        addOutPair(pairList, outActList, deep + 1);
    }

    private void addEachOutPair(List<String[]> pariList, PvmActivity act1, PvmActivity act2) {
        String type1 = (String) act1.getProperty("type");
        String type2 = (String) act2.getProperty("type");
        List<String> sarr1 = new ArrayList<>();
        List<String> sarr2 = new ArrayList<>();
        if (type1.endsWith("Task")) {
            sarr1.add((String) act1.getProperty("name"));
        } else if (type1.equals("exclusiveGateway")) {
            addEGTasks(sarr1, act1.getOutgoingTransitions());
        }
        if (type2.endsWith("Task")) {
            sarr2.add((String) act2.getProperty("name"));
        } else if (type2.equals("exclusiveGateway")) {
            addEGTasks(sarr2, act2.getOutgoingTransitions());
        }

        int s1 = sarr1.size();
        int s2 = sarr2.size();
        for (int i = 0; i < s1; i++) {
            for (int j = 0; j < s2; j++) {
//                String id1 = sarr1.get(i);
//                String id2 = sarr2.get(j);
//                if(id1.compareTo(id2) > 0) {
//                    pariList.add(new String[]{id2, id1});
//                } else {
//                    pariList.add(new String[] {id1, id2});
//                }
                pariList.add(new String[]{sarr1.get(i), sarr2.get(j)});
            }
        }
    }

    private void addEGTasks(List<String> sarr, List<PvmTransition> outs) {
        for (PvmTransition out : outs) {
            PvmActivity act = out.getDestination();
            String type = (String) act.getProperty("type");
            if (type.endsWith("Task")) {
                sarr.add((String) act.getProperty("name"));
            } else {
                System.err.println("RhoAutoRunner: addEGTasks meet wrong type of exclusive gateway, ignored.");
            }
        }
    }

    public void runProcessByDefinitionKeyName(RuntimeService runtimeService, String processDefinitionKeyName) throws Exception {
        if (this.repositoryService == null) {
            throw new Exception("RhoAutoRunner: please init repositoryService first!");
        }

        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKeyName).orderByProcessDefinitionVersion().desc()    .list();

        if (processDefinitionList.size() == 0) {
            throw new Exception("RhoAutoRunner: no process definition found!");
        }

        ProcessDefinition processDefinition = processDefinitionList.get(0);

        System.out.println("process def: " + processDefinition.getId());

        ProcessDefinitionEntity pd = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processDefinition.getId());

        List<ActivityImpl> activityList = pd.getActivities();

        RhoExpressionManager expressionManager = new RhoExpressionManager();

        for (ActivityImpl ai : activityList) {
            Map<String, Object> proMap = ai.getProperties();
            if (proMap == null || !proMap.containsKey("type")) {
                continue;
            }
            String type = (String) proMap.get("type");
            if (type.equals("inclusiveGateway")) {
                throw new Exception("inclusive gateway is not support!");
            }
            if (type.equals("exclusiveGateway")) {
                List<PvmTransition> outgoingTransitions = ai.getOutgoingTransitions();
                if(outgoingTransitions.size() <= 1) {
                    continue;
                }
                RhoExpressionHolder expressionHolder = new RhoExpressionExclusiveHolder();
                for (PvmTransition outgoingTransition : outgoingTransitions) {
                    TransitionImpl ti = (TransitionImpl) outgoingTransition;
                    Map<String, Object> tip = ti.getProperties();
//                    if (tip == null || !tip.containsKey("condition")) {
//                        continue;
//                    }
//                    Object ct = tip.get("condition");
//                    if (ct == null || !(ct instanceof UelExpressionCondition)) {
//                        continue;
//                    }
                    RhoExpressionCondition rec = new RhoExpressionCondition();
                    expressionHolder.addCondition(rec);
                    tip.put("condition", rec);
                }

                expressionManager.addExpressionHolder(expressionHolder);

            } else if (type.equals("parallelGateway")) {
                dealParallelGateway(ai);
            }
        }


        /**
         * debugMax和debugIdx是为了防止陷入死循环。
         */
        int debugMax = 10000;
        int debugIdx = 0;
        while (debugIdx < debugMax && !expressionManager.isFinish()) {
            expressionManager.run();
            expressionManager.printTo(System.out);
            runtimeService.startProcessInstanceById(processDefinition.getId());
            debugIdx++;
        }

        if (debugIdx == debugMax) {
            throw new Exception("RhoAutoRunner: expression manager go into infinite loop");
        }

        if(parallelCache.size() != 0) {
            BufferedWriter storeWriter = rhoEventLoggerBean.getRhoEventLogger().getStoreWriterByProcessDefId(processDefinition.getId());
            if (storeWriter == null) {
                throw new Exception("RhoAutoRunner: no buffered writer found");
            }

            dealSotredLogList(storeWriter);

            storeWriter.close();
        }

    }

    private List<List<RhoEventLogEntity>> storedLogList = new ArrayList<>();

    @Override
    public void onBeforeStoreLog(List<RhoEventLogEntity> rhoEventLogEntityList, RhoEventLogger rhoEventLogger) {
//        System.out.println("before store log event");
//        storedLogList.add(rhoEventLogEntityList);

        // java <= 7 不支持内嵌函数真是忧伤。。。
        // 为了写代码简单，这里使用了贪婪检测。性能很低。
        // 但这个AutoRunner本身只是用来测试时使用，
        // 生产环境中是不会用到这个东西的。
        // 生产环境中的日志是实际流程的记录，而不是靠自动自成的。
        // 这个自动生成的算法，是为了在满足rho-complete条件下，
        // 尽可能的少生成日志（基本就是正好满足rho-complete条件的最小日志trace条数）,
        // 这个算法使用了很复杂效率很低的逻辑。
        if(parallelCache.size() == 0) {
            return;
        }
//        System.out.print("ori: ");
//        ppp(rhoEventLogEntityList);
//        boolean t;
//        do {
        _t(rhoEventLogEntityList);
//            if(t) {
//                System.out.print("cto: ");
//                ppp(rhoEventLogEntityList);
//            }
//        } while (t);

    }

    private void ppp(List<RhoEventLogEntity> rhoEventLogEntityList) {
        for(RhoEventLogEntity eventLogEntity: rhoEventLogEntityList) {
            System.out.print(eventLogEntity.toString());
            System.out.print(",");
        }
        System.out.println();
    }

    private boolean _t(List<RhoEventLogEntity> rhoEventLogEntityList) {
        Map<String, ActualE> tmpMap = new HashMap<>();
        List<String> kList = new ArrayList<>();

        for (int i = 0; i < rhoEventLogEntityList.size(); i++) {
            RhoEventLogEntity eventLogEntity = rhoEventLogEntityList.get(i);
            String pt;
            String ct;
            pt = eventLogEntity.getPreTask();
            if (!parallelCache.containsKey(pt)) {
                continue;
            }
            ct = eventLogEntity.getCurTask();
            ActualE tmpList;
            if (tmpMap.containsKey(pt)) {
                tmpList = tmpMap.get(pt);
            } else {
                tmpList = new ActualE();
                tmpMap.put(pt, tmpList);
            }
            tmpList.list.add(ct);
            tmpList.idx.add(i);
            if (tmpList.list.size() >= 2) {
                kList.add(pt);
            }
        }

        for (String k : kList) {

            ActualE ae = tmpMap.get(k);
            List<String> ll = ae.list;
            List<Integer> li = ae.idx;
            ParallelE pe = parallelCache.get(k);
            List<String[]> ssList = pe.list;

            for (int i = 0; i < ll.size() - 1; i++) {
                for (int j = i + 1; j < ll.size(); j++) {
                    String id_i = ll.get(i);
                    String id_j = ll.get(j);
//                    String id_min, id_max;
//                    if(id_i.compareTo(id_j) > 0) {
//                        id_max = id_i;
//                        id_min = id_j;
//                    } else {
//                        id_max = id_j;
//                        id_min = id_i;
//                    }
                    for (String[] pss : ssList) {
//                        if(pss[0].equals(id_min) && pss[1].equals(id_max)) {
                        if ((pss[0].equals(id_i) && pss[1].equals(id_j))
                                || (pss[0].equals(id_j) && pss[1].equals(id_i))) {

                            ssList.remove(pss);
                            if (ssList.size() == 0) {
                                parallelCache.remove(k);
                            } else {
                                pe.trace = new ArrayList<>();
                                pe.trace.addAll(rhoEventLogEntityList);
                            }
                            int idx_j = li.get(j);
                            int idx_i = li.get(i);
                            if (idx_j > idx_i + 1) {
                                //move j to next i, that is, i+1
                                Collections.swap(rhoEventLogEntityList, idx_i + 1, idx_j);
                            } else if(idx_i > idx_j + 1) {
                                Collections.swap(rhoEventLogEntityList, idx_j+1, idx_i);
                            }

                            return true;
                        }
                    }
                }

            }
        }

        return false;
    }

    private void dealSotredLogList(BufferedWriter bufferedWriter) throws IOException {
        System.out.println("append more parallel.");
        for(String k : parallelCache.keySet()) {
            ParallelE pe = parallelCache.get(k);
            List<RhoEventLogEntity> rhoEventLogEntityList = pe.trace;
            List<String[]> pairList = pe.list;

            for(String[] pair : pairList) {
                RhoEventLogEntity r1 = null, r2 = null;
                int idx1 = -1, idx2 = -1;

                for (int i = 0; i < rhoEventLogEntityList.size(); i++) {
                    RhoEventLogEntity logEntity = rhoEventLogEntityList.get(i);
                    String ct = logEntity.getCurTask();
                    String pt = logEntity.getPreTask();
                    if (!pt.equals(k)) {
                        continue;
                    }
                    if (ct.equals(pair[0])) {
                        r1 = logEntity;
                        idx1 = i;
                    } else if (ct.equals(pair[1])) {
                        r2 = logEntity;
                        idx2 = i;
                    }
                }

                if(r1!=null && r2!=null) {
                    List<RhoEventLogEntity> newList = new ArrayList<>();
                    newList.addAll(rhoEventLogEntityList);

                    if(idx1> idx2+1) {
                        Collections.swap(newList, idx2+1, idx1);
                    } else if(idx2 > idx1+1) {
                        Collections.swap(newList, idx1+1, idx2);
                    }

                    StringBuilder sb = new StringBuilder();
                    Boolean first = true;
                    for(RhoEventLogEntity r : newList) {
                        if(!first) {
                            sb.append(',');
                        } else {
                            first = false;
                        }
                        sb.append('[').append(r.getPreTask()).append(']').append(r.getCurTask());
                    }
                    sb.append("\r\n");
                    appendLog(bufferedWriter, sb.toString());
                }



            }

        }
    }

    private synchronized static void appendLog(BufferedWriter writer, String line) throws IOException {
        writer.append(line);
        writer.flush();
    }
}

class ActualE {
    public List<String> list = new ArrayList<>();
    public List<Integer> idx = new ArrayList<>();
}

class ParallelE {
    public List<String[]> list = new ArrayList<>();
    public List<RhoEventLogEntity> trace = null;
}