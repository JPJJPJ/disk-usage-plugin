package hudson.plugins.disk_usage;

import hudson.Extension;
import hudson.model.*;
import hudson.plugins.disk_usage.unused.DiskUsageNotUsedDataCalculationThread;
import hudson.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author: <a hef="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Extension
public class DiskUsageProjectActionFactory extends TransientProjectActionFactory implements Describable<DiskUsageProjectActionFactory> {

    @Override
    public Collection<? extends Action> createFor(AbstractProject job) {
        ProjectDiskUsageAction action = new ProjectDiskUsageAction(job);
        return Collections.singleton(action);
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public Descriptor<DiskUsageProjectActionFactory> getDescriptor() {
        return DESCRIPTOR;
    }

    public static class DescriptorImpl extends Descriptor<DiskUsageProjectActionFactory> {

        public DescriptorImpl() {
            load();
        }

        private String countIntervalBuilds = "0 */6 * * *"; 
    
        private boolean calculationBuilds = true;
    
        private String countIntervalJobs = "0 */6 * * *";
    
        private boolean calculationJobs = true;
    
        private String countIntervalWorkspace ="0 */6 * * *";
    
        private boolean calculationWorkspace = true;
    
        private boolean checkWorkspaceOnSlave = false;
        
        private String countNotUsedData ="0 */6 * * *";
        
        private boolean calculationNotUsedData = false;
    
        private String email;
    
        private String jobSize;
    
        private String buildSize;
    
        private String allJobsSize;
    
        private String jobWorkspaceExceedSize;    
        
        private boolean showFreeSpaceForJobDirectory = true;
        
        private List<String> excludedJobs = new ArrayList<String>();
    
        private Long diskUsageBuilds = 0l;
        private Long diskUsageJobsWithoutBuilds = 0l;
        private Long diskUsageWorkspaces = 0l;
        private Long diskUsageLockedBuilds = 0l;
    
        private boolean showGraph = true;
        private int historyLength = 183;
        List<DiskUsageOvearallGraphGenerator.DiskUsageRecord> history = new LinkedList<DiskUsageOvearallGraphGenerator.DiskUsageRecord>(){
				private static final long serialVersionUID = 1L;

				@Override
				public boolean add(DiskUsageOvearallGraphGenerator.DiskUsageRecord e) {
					boolean ret = super.add(e);
					if(ret && this.size() > historyLength){
						this.removeRange(0, this.size() - historyLength);
					}
					return ret;
				}
			};

        // Timeout for a single Project's workspace analyze (in mn)
        private int timeoutWorkspace = 5;
        
    public Long getCashedGlobalBuildsDiskUsage(){
        return diskUsageBuilds;
    }
    
    public Long getCashedGlobalJobsDiskUsage(){
        return (diskUsageBuilds + diskUsageJobsWithoutBuilds);
    }
    
    public Long getCashedGlobalJobsWithoutBuildsDiskUsage(){
        return diskUsageJobsWithoutBuilds;
    }
    
    public Long getCashedGlobalLockedBuildsDiskUsage(){
     return diskUsageLockedBuilds;   
    }
    
    public Long getCashedGlobalWorkspacesDiskUsage(){
        return diskUsageWorkspaces;
    }
    
    public Long getJobWorkspaceExceedSize(){
        return DiskUsageUtil.getSizeInBytes(jobWorkspaceExceedSize);
    }
    
    public String getJobWorkspaceExceedSizeInString(){
        return jobWorkspaceExceedSize;
    }
    
    public boolean isShowGraph() {
        return showGraph;
    }

    public void setShowGraph(Boolean showGraph) {
        this.showGraph = showGraph;
    }

    public int getHistoryLength() {
        return historyLength;
    }

    public void setHistoryLength(Integer historyLength) {
        this.historyLength = historyLength;
    }

    public List<DiskUsageOvearallGraphGenerator.DiskUsageRecord> getHistory(){
        return history;
    }

    public String getCountIntervalForBuilds(){
    	return countIntervalBuilds;
    }
    
    public String getCountIntervalForJobs(){
    	return countIntervalJobs;
    }
    
    public String getCountIntervalForWorkspaces(){
    	return countIntervalWorkspace;
    }
    
    public String getCountIntervalForNotUsedData(){
        return countNotUsedData;
    }
    
    public boolean getCheckWorkspaceOnSlave(){
        return checkWorkspaceOnSlave;
    }
    
    public void setCheckWorkspaceOnSlave(boolean check){
        checkWorkspaceOnSlave = check;
    }
    
    public void setExcludedJobs(List<String> excludedJobs){
        this.excludedJobs = excludedJobs;
    }
    
     public boolean isCalculationWorkspaceEnabled(){
        return calculationWorkspace;
    }
    
    public boolean isCalculationBuildsEnabled(){
        return calculationBuilds;
    }
    
    public boolean isCalculationJobsEnabled(){
        return calculationJobs;
    }
    
    public boolean isCalculationNotUsedDataEnabled(){
        return calculationNotUsedData;
    }
    
    public boolean warnAboutJobWorkspaceExceedSize(){
        return jobWorkspaceExceedSize!=null;
    }
    
    public boolean warnAboutAllJobsExceetedSize(){
        return allJobsSize!=null;
    }
    
    public boolean warnAboutBuildExceetedSize(){
        return buildSize!=null;
    }
    
    public boolean warnAboutJobExceetedSize(){
        return jobSize!=null;
    }   

    public String getEmailAddress(){
        return email;
    }
    
    public boolean warningAboutExceededSize(){
        return email!=null;
    }
    
    public Long getAllJobsExceedSize(){
        return DiskUsageUtil.getSizeInBytes(allJobsSize);
    }
    
    public Long getBuildExceedSize(){
        return DiskUsageUtil.getSizeInBytes(buildSize);
    }
    
    public Long getJobExceedSize(){
        return DiskUsageUtil.getSizeInBytes(jobSize);
    }
    
    public String getAllJobsExceedSizeInString(){
        return allJobsSize;
    }
    
    public String getBuildExceedSizeInString(){
        return buildSize;
    }
    
    public String getJobExceedSizeInString(){
        return jobSize;
    }

    public boolean addHistory(DiskUsageOvearallGraphGenerator.DiskUsageRecord e) {
        boolean ok = history.add(e);
        save();
        return ok;
    }
    
    public void enableBuildsDiskUsageCalculation(){
        calculationBuilds=true;
    }
    
    public void disableBuildsDiskUsageCalculation(){
        calculationBuilds=false;
    }
    
    public void enableJobsDiskUsageCalculation(){
        calculationJobs=true;
    }
    
    public void disableJobsDiskUsageCalculation(){
        calculationJobs=false;
    }
    
    public void enableWorkspacesDiskUsageCalculation(){
        calculationWorkspace=true;
    }
    
    public void disableWorkspacesDiskUsageCalculation(){
        calculationWorkspace=false;
    }
    
    public String getUnit(String unit){
        if(unit==null)
            return null;
        return unit.split(" ")[1];
    }
    
    public String getValue(String size){
        if(size==null)
            return null;
        return size.split(" ")[0];
    }


    @Override
    public String getDisplayName() {
        return Messages.DisplayName();
    }


    @Override
    public DiskUsageProjectActionFactory newInstance(StaplerRequest req, JSONObject formData) {
        return new DiskUsageProjectActionFactory();
    }


    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) {
        Jenkins.getInstance().checkPermission(Permission.CONFIGURE);
        JSONObject form;
        try {
            form = req.getSubmittedForm();
        } catch (ServletException ex) {
            Logger.getLogger(DiskUsageProjectActionFactory.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        //workspaceTimeOut = form.getInt("countInterval");
        checkWorkspaceOnSlave = form.getBoolean("checkWorkspaceOnSlave");
        configureBuildsCalculation(form);
        configureJobsCalculation(form);
        configureWorkspacesCalculation(form);  
        configureNotUsedDataCalculation(form);
        String excluded = form.getString("excludedJobs");
        excludedJobs = DiskUsageUtil.parseExcludedJobsFromString(excluded);
        if(form.containsKey("warnings")){
            JSONObject warnings = form.getJSONObject("warnings");
            email = warnings.getString("email");           
            if(email!=null){
                allJobsSize = warnings.containsKey("jobsWarning")? (warnings.getJSONObject("jobsWarning").getInt("allJobsSize") + " " + warnings.getJSONObject("jobsWarning").getString("JobsSizeUnit")) : null;
                buildSize = warnings.containsKey("buildWarning")? (warnings.getJSONObject("buildWarning").getInt("buildSize") + " " + warnings.getJSONObject("buildWarning").getString("buildSizeUnit")) : null;
                jobSize = warnings.containsKey("jobWarning")? (warnings.getJSONObject("jobWarning").getInt("jobSize") + " " + warnings.getJSONObject("jobWarning").getString("jobSizeUnit")) : null;
                jobWorkspaceExceedSize = warnings.containsKey("workspaceWarning")? (warnings.getJSONObject("workspaceWarning").getInt("jobWorkspaceExceedSize") + " " + warnings.getJSONObject("workspaceWarning").getString("jobWorkspaceExceedSizeUnit")) : null;
            }
        }
        showGraph = form.getBoolean("showGraph");
                    String histlen = req.getParameter("historyLength");
                    if(histlen != null && !histlen.isEmpty()){
                        historyLength = Integer.parseInt(histlen);
                    }
       timeoutWorkspace = form.getInt("timeoutWorkspace");
       showFreeSpaceForJobDirectory = form.getBoolean("showFreeSpaceForJobDirectory");
        save();
        return true;
    }
    
    public void onRenameJob(String oldName, String newName){
        if(excludedJobs.contains(oldName)){
            excludedJobs.remove(oldName);
            excludedJobs.add(newName);
        }      
    }
    
    public void onDeleteJob(AbstractProject project){
        String name = project.getName();
        if(excludedJobs.contains(name)){
            excludedJobs.remove(name);
        } 
    }
    
    public boolean isExcluded(AbstractProject project){
        return excludedJobs.contains(project.getName());
    }
    
    public String getExcludedJobsInString(){
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for(String name: excludedJobs){
            if(first){
                first= false;
            }
            else{
                builder.append(", ");
            }
            builder.append(name); 
        }
        return builder.toString();
    }
    
    private void configureBuildsCalculation(JSONObject form){
        boolean oldCalculationBuilds = calculationBuilds;
        String oldCountIntervalBuilds = countIntervalBuilds;
        calculationBuilds = form.containsKey("calculationBuilds");
        countIntervalBuilds = calculationBuilds? form.getJSONObject("calculationBuilds").getString("countIntervalBuilds") : "0 */6 * * *";
        BuildDiskUsageCalculationThread buildCalculation = AperiodicWork.all().get(BuildDiskUsageCalculationThread.class);
        if(!oldCountIntervalBuilds.equals(countIntervalBuilds) || oldCalculationBuilds!=calculationBuilds)
            buildCalculation.reschedule();
    }
    
    private void configureJobsCalculation(JSONObject form){
        boolean oldCalculationJobs = calculationJobs;
        String oldcountIntervalJobs = countIntervalJobs;
        calculationJobs = form.containsKey("calculationJobs");
        countIntervalJobs = calculationJobs? form.getJSONObject("calculationJobs").getString("countIntervalJobs") : "0 */6 * * *";
        JobWithoutBuildsDiskUsageCalculation jobCalculation = AperiodicWork.all().get(JobWithoutBuildsDiskUsageCalculation.class);
        if(!oldcountIntervalJobs.equals(countIntervalJobs) || oldCalculationJobs!=calculationJobs)
            jobCalculation.reschedule();
    }
    
    private void configureWorkspacesCalculation(JSONObject form){
        boolean oldCalculationWorkspace = calculationWorkspace;
        String oldCountIntervalWorkspace = countIntervalWorkspace;
        calculationWorkspace = form.containsKey("calculationWorkspace");
        countIntervalWorkspace = calculationWorkspace? form.getJSONObject("calculationWorkspace").getString("countIntervalWorkspace") : "0 */6 * * *";
        WorkspaceDiskUsageCalculationThread workspaceCalculation = AperiodicWork.all().get(WorkspaceDiskUsageCalculationThread.class);
        if(!oldCountIntervalWorkspace.equals(countIntervalWorkspace) || oldCalculationWorkspace!=calculationWorkspace)
            workspaceCalculation.reschedule();
    }
    
    private void configureNotUsedDataCalculation(JSONObject form){
        boolean oldNotUsedData = calculationNotUsedData;
        String oldCountIntervalNotUsedData = countNotUsedData;
        calculationNotUsedData = form.containsKey("calculationNotUsedData");
        countNotUsedData = calculationNotUsedData? form.getJSONObject("calculationNotUsedData").getString("countIntervalNotUsedData") : "0 */6 * * *";
        DiskUsageNotUsedDataCalculationThread notUsedDataCalculation = AperiodicWork.all().get(DiskUsageNotUsedDataCalculationThread.class);
        if(!oldCountIntervalNotUsedData.equals(countNotUsedData) || oldNotUsedData!=calculationNotUsedData)
            notUsedDataCalculation.reschedule();
    }

    public int getTimeoutWorkspace() {
        return timeoutWorkspace;
    }
    
    public boolean getShowFreeSpaceForJobDirectory(){
        return showFreeSpaceForJobDirectory;
    }

    public void setTimeoutWorkspace(Integer timeoutWorkspace) {
        this.timeoutWorkspace = timeoutWorkspace;
    }
}


}
