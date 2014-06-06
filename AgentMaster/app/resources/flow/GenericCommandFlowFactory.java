package resources.flow;

import java.util.HashMap;
import java.util.Map;

import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.session.exception.FlowExecutionException;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.StepBuilder;
import org.lightj.session.step.TaskFactoryStepExecution.IFlowContextTaskFactory;
import org.lightj.session.step.TaskFactoryStepExecution.TaskInFlow;
import org.lightj.task.ExecutableTask;
import org.lightj.task.ITaskEventHandler;
import org.lightj.task.NoopTask;
import org.lightj.task.SimpleTaskEventHandler;
import org.lightj.task.Task;
import org.lightj.task.TaskResult;
import org.lightj.task.TaskResultEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import resources.UserDataProvider;
import resources.command.ICommand;
import controllers.Commands;

@SuppressWarnings("rawtypes")
@Configuration
public class GenericCommandFlowFactory {
	
	public @Bean @Scope("prototype") static GenericCommandFlow genericCommandFlow() {
		return new GenericCommandFlow();
	}

	@Bean 
	@Scope("prototype")
	public static IFlowStep executeCommandStep() {
		
		return new StepBuilder().executeTasks(new IFlowContextTaskFactory<GenericCommandFlowContext>() {

			@Override
			public TaskInFlow<GenericCommandFlowContext> createTaskInFlow(
					final GenericCommandFlowContext context, int sequence) 
			{
				
				TaskInFlow<GenericCommandFlowContext> taskInFlow = null;
				
				try {

					if (context.hasMoreCommand()) {
						String commandName = context.getCurrentCommand();
						ICommand command = UserDataProvider.getCommandConfigs().getCommandByName(commandName);
						Map<String, String> userData = context.getCommandUserData(commandName);
						HttpTaskRequest req = Commands.createTaskByRequest(
															context.getHosts(), 
															command, 
															new HashMap<String, String>(1),
															userData);
						ExecutableTask task = HttpTaskBuilder.buildTask(req);

						
						ITaskEventHandler handler = new SimpleTaskEventHandler<GenericCommandFlowContext>() {
							@Override
							public void executeOnResult(GenericCommandFlowContext ctx, Task task, TaskResult result) {
								try {
									if (result.getStatus() != TaskResultEnum.Success) {
										ctx.addFailedHost(result.getResultDetail("host"));
									}
								} catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
							@Override
							public TaskResultEnum executeOnCompleted(
									GenericCommandFlowContext ctx,
									Map<String, TaskResult> results) {
								ctx.incCurCmdIdx();
								return null;
							}
						};

						taskInFlow = new TaskInFlow<GenericCommandFlowContext>(context.getBatchOption(), handler, task);
					}
					else {
						taskInFlow = new TaskInFlow<GenericCommandFlowContext>(null, null, new NoopTask());
					}
					
				} catch (Exception e) {
					throw new FlowExecutionException(e);
				}
				
				return taskInFlow;
			}

		}).getFlowStep();
		
	}
	
}
