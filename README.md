# camunda-complexvariable-c8

In Camunda8, the Worker pattern is the only way to integrate an external system.

# Test it
Generate a C8 Cloud Cluster, or install a local Zeebe in Docker.
Update src/main/resources/application.properties accordingly.

Deploy the process "src/main/resources/CompleteVariables.bpmn"

Start the worker org;camjunda.complexevariables.c8.worker.WorkerVariables

# JSON
You can save a variable as a String. This variable can be a JSON, and an object can be saved in this representation.
See updateJsonBased method()

# Direct variable
You can saved directly an object via the API

````
Customer customer = new Customer();
....
Map<String,Object> variables = new HashMap<>()
variables.put("myCustomer", customer);

completeJobCommandStep.variables(variables)
                      .send()   
````
Camunda saved the variable as a JSON representation.
So, to read it, a GSon is necessary
````
 Object customerAsMap = job.getVariablesAsMap().get("myCustomer");
if ( ! (customerAsMap instanceof Map)) 
    return;

Gson gson = new Gson();
JsonElement jsonElement = gson.toJsonTree((Map) customerAsMap);
Customer customer = gson.fromJson(jsonElement, Customer.class);
`````
# POJO
Zeebe client has a POJO method. You save the class

````
Customer customer = new Customer();
...
completeJobCommandStep.variables(customer)
                      .send()
````
And you can retrieve it as
```
Customer customer = job.getVariablesAsType(Customer.class);
````
but then you save / get all variables of the process in the class. If you want to add a variable, you must add it in the Customer class.


# Forms
An embbeded form in Camunda 7  

# More information
here some information to migrate a C7 process to C8
Using https://github.com/camunda-community-hub/camunda-platform-to-cloud-migration

Here some information about the Zeebe client
https://github.com/camunda-community-hub/spring-zeebe

Create an embeded form
https://github.com/camunda/camunda-bpm-examples/tree/master/usertask/task-form-embedded