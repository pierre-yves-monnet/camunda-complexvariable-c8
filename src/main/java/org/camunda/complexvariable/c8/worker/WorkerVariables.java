package org.camunda.complexvariable.c8.worker;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.camunda.complexvariable.c8.data.Customer;
import org.camunda.complexvariable.c8.data.CustomerUpdate;
import org.camunda.complexvariable.c8.process.complexvariables.ComplexVariableConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableZeebeClient
public class WorkerVariables {
    private final Logger logger = LoggerFactory.getLogger(WorkerVariables.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(WorkerVariables.class, args);

    }


    @ZeebeWorker(type = "complex-variables")
    public void handleWorkerVariable(final JobClient client, final ActivatedJob job) {
        logger.info("WorkerVariables.handleWorkerVariable : >>>>>>>>>>> start [" + job.getKey() + "]");
        Map<String, Object> variables = new HashMap<>();
        List<String> logs = new ArrayList<>();
        try {
            CompleteJobCommandStep1 completeJobCommandStep = client.newCompleteCommand(job.getKey());
            logs.add(updateJsonBased(job, variables));

            // We don't want the POJO method at this moment. See below.
            // logs.add(updatePojoBased(job, variables, completeJobCommandStep));

            logs.add(updateDirectBased(job, variables));

            String logSt = (String) job.getVariablesAsMap().get(ComplexVariableConstant.PROCESS_VARIABLE_LOGS);
            logSt = (logSt == null ? "" : logSt) + logs.stream().collect(Collectors.joining(", "));
            variables.put(ComplexVariableConstant.PROCESS_VARIABLE_LOGS, logSt);

            completeJobCommandStep.variables(variables)
                    .send()
                    .exceptionally((throwable -> {
                        throw new RuntimeException("Could not complete job", throwable);
                    }));

        } catch (Exception e) {
            logger.info("WorkerVariables.handleWorkerVariable Exception [" + e + "]");

        }
        logger.info("WorkerVariables.handleWorkerVariable : >>>>>>>>>>>> end [" + job.getKey() + "]");

    }

    /**
     * Access and modify a JSON variables
     *
     * @param job manipulate task
     * @return a status
     */
    public String updateJsonBased(ActivatedJob job, Map<String, Object> variables) {
        try {
            Object currentCustomer = job.getVariablesAsMap().get(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JSON);
            Customer customer;
            Gson gson = new Gson();
            if (currentCustomer != null) {
                String customerJson = (String) job.getVariablesAsMap().get(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JSON);
                JsonParser parser = new JsonParser();
                JsonObject object = (JsonObject) parser.parse(customerJson);// response will be the json String
                customer = gson.fromJson(object, Customer.class);
            } else
                customer = new Customer();
            customer = CustomerUpdate.update(customer, "WorkHood", "Worker");

            String customerJson = gson.toJson(customer);

            variables.put(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JSON, customerJson);
            return "Json Worker OK";
        } catch (Exception e) {
            logger.info("WorkerVariables.executeInJson Exception [" + e + "]");
            return "Json Worker failed [" + e + "]";
        }
    }

    /**
     * Access and modify All Variables in one class
     * Using this method imply all variables are part of the Customer class. You can't ask to upload part of variables, and if
     * you want to add a new variable, it must be an attribute in the class.
     * This is very usefull for specific process (easy to manipulate all variables), not for reusable worker
     *
     * @param job manipulate task
     * @return a status
     */
    public String updatePojoBased(ActivatedJob job, Map<String, Object> variables, CompleteJobCommandStep1 completeJobCommandStep) {
        try {
            Customer customer = job.getVariablesAsType(Customer.class);

            if (customer == null) {
                customer = new Customer();
            }
            customer = CustomerUpdate.update(customer, "WorkHood", "Worker");

            completeJobCommandStep.variables(customer);

            return "Java POJO OK";
        } catch (Exception e) {
            logger.info("WorkerVariables.updatePojoBased Exception [" + e + "]");
            return "Java POJO failed [" + e + "]";
        }
    }

    /**
     * Access and modify directly a variable
     * in Zeebee, a object is serialized in a Map. So, to get it back
     *
     * @param job manipulate task
     * @return a status
     */
    public String updateDirectBased(ActivatedJob job, Map<String, Object> variables) {
        try {
            Object currentCustomer = job.getVariablesAsMap().get(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER);
            Customer customer;
            if (currentCustomer instanceof Map) {
                Gson gson = new Gson();

                JsonElement jsonElement = gson.toJsonTree((Map) currentCustomer);
                customer = gson.fromJson(jsonElement, Customer.class);

            } else {
                customer = new Customer();
            }

            customer = CustomerUpdate.update(customer, "WorkHood", "Worker");

            // a JAVA serialization will be done behind the scene
            variables.put(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER, customer);
            return "Direct Worker OK";
        } catch (Exception e) {
            logger.info("WorkerVariables.executeDirect Exception [" + e + "]");
            return "Direct Worker failed [" + e + "]";

        }
    }
}
