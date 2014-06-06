package resources.agent.flow.installagent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.session.exception.FlowExecutionException;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.StepBuilder;
import org.lightj.session.step.TaskFactoryStepExecution.IFlowContextTaskFactory;
import org.lightj.session.step.TaskFactoryStepExecution.TaskInFlow;
import org.lightj.task.BatchTask;
import org.lightj.task.ExecutableTask;
import org.lightj.task.GroupTask;
import org.lightj.task.ITaskEventHandler;
import org.lightj.task.NoopTask;
import org.lightj.task.SimpleTaskEventHandler;
import org.lightj.task.Task;
import org.lightj.task.TaskExecutionException;
import org.lightj.task.TaskResult;
import org.lightj.task.TaskResultEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import resources.UserDataProvider;
import resources.command.ICommand;
import resources.utils.SshUtils;
import controllers.Commands;

@SuppressWarnings("rawtypes")
@Configuration
public class InstallAgentFlowFactory {
	
	public @Bean @Scope("prototype") static InstallAgentFlow installAgentFlow() {
		return new InstallAgentFlow();
	}

	@Bean 
	@Scope("prototype")
	public static IFlowStep installAgentStep() {
		
		return new StepBuilder().executeTasks(new IFlowContextTaskFactory<InstallAgentFlowContext>() {

			@Override
			public TaskInFlow<InstallAgentFlowContext> createTaskInFlow(
					final InstallAgentFlowContext context, int sequence) 
			{
				
				TaskInFlow<InstallAgentFlowContext> taskInFlow = null;
				
				try {
					GroupTask<ExecutableTask> task = new GroupTask<ExecutableTask>() {

						@Override
						public List<ExecutableTask> getTasks() {
							List<ExecutableTask> tasks = new ArrayList<ExecutableTask>();

							for (final String host : context.getHosts()) {
								
								tasks.add(new ExecutableTask() {
									
									@Override
									public TaskResult execute() throws TaskExecutionException {
										
										Session session = null;
										try {
											// install agent via ssh
											session = SshUtils.connectViaKey(context.getSshUser(), host, 22, new SshUtils.IdentityKey() {
												
												@Override
												public String privateKeyFile() {
													return context.getPrivateKeyFile();
												}
												
												@Override
												public String passphrase() {
													return context.getPassPhrase();
												}
											});
											
											String agentInstallCmd = String.format("curl -sS 'https://raw.githubusercontent.com/yubin154/cronusagent" +
													"/master/agent/scripts/agent_install/install_agent' | sudo pkg_ver=%s target_dir=%s bash", 
													context.getVersion(), context.getInstallRoot());
											SshUtils.execCmd(session, agentInstallCmd);
											
										} 
										catch (JSchException e) {
											throw new TaskExecutionException(e);
										} catch (IOException e) {
											throw new TaskExecutionException(e);
										} 
										finally {
											SshUtils.disconnect(session);
										}
										
										return this.succeeded();
									}
									
								});
							}
							return tasks;
						}
					};

					ITaskEventHandler handler = new SimpleTaskEventHandler<InstallAgentFlowContext>() {
						@Override
						public void executeOnResult(InstallAgentFlowContext ctx, Task task, TaskResult result) {
							try {
								if (result.getStatus() != TaskResultEnum.Success) {
									ctx.addFailedHost(result.getResultDetail("host"));
								}
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}

					};

					taskInFlow = new TaskInFlow<InstallAgentFlowContext>(context.getBatchOption(), handler, task);

				} catch (Exception e) {
					throw new FlowExecutionException(e);
				}
				
				return taskInFlow;
			}

		}).getFlowStep();
		
	}
	
}
