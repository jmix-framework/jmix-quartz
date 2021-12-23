package io.jmix.quartz.screen.jobs;

import com.google.common.base.Strings;
import io.jmix.quartz.model.JobDataParameterModel;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.model.TriggerModel;
import io.jmix.quartz.service.QuartzDataProvider;
import io.jmix.quartz.service.QuartzService;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import io.jmix.quartz.util.QuartzUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("JobModel.edit")
@UiDescriptor("job-model-edit.xml")
@EditedEntityContainer("jobModelDc")
public class JobModelEdit extends StandardEditor<JobModel> {

    @Autowired
    private QuartzDataProvider quartzDataProvider;
    @Autowired
    private QuartzService quartzService;
    @Autowired
    protected QuartzUtils quartzUtils;
    @Autowired
    private CollectionContainer<JobDataParameterModel> jobDataParamsDc;
    @Autowired
    private CollectionContainer<TriggerModel> triggerModelDc;
    @Autowired
    private ComboBox<String> jobGroupField;
    @Autowired
    private ComboBox<String> jobClassField;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        //allow editing only not active job
        setReadOnly(getEditedEntity().getIsActive());

        List<String> jobGroupNames = quartzDataProvider.getJobGroupNames();
        jobGroupField.setOptionsList(jobGroupNames);
        jobGroupField.setEnterPressHandler(enterPressEvent -> {
            String newJobGroupName = enterPressEvent.getText();
            if (!Strings.isNullOrEmpty(newJobGroupName) && !jobGroupNames.contains(newJobGroupName)) {
                jobGroupNames.add(newJobGroupName);
                jobGroupField.setOptionsList(jobGroupNames);
            }
        });

        jobClassField.setOptionsList(quartzUtils.getExistedJobsClassNames());

        String jobName = getEditedEntity().getJobName();
        String jobGroup = getEditedEntity().getJobGroup();
        if (!Strings.isNullOrEmpty(jobName)) {
            jobDataParamsDc.setItems(quartzDataProvider.getDataParamsOfJob(jobName, jobGroup));
            triggerModelDc.setItems(quartzDataProvider.getTriggersOfJob(jobName, jobGroup));
        }
    }

    @Subscribe("windowCommitAndClose")
    public void onWindowCommitAndClose(Action.ActionPerformedEvent event) {
        JobModel jobModel = getEditedEntity();
        List<JobDataParameterModel> jobDataParameterModels = jobDataParamsDc.getItems();
        List<TriggerModel> triggerModels = triggerModelDc.getItems();
        quartzService.updateQuartzJob(jobModel, jobDataParameterModels, triggerModels);
    }

}