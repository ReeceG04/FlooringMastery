package org.example;

import org.example.controller.FlooringMasteryController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //Spring builds the entire project here by reading the applicationContext.xml
        //and wiring constructor arguments together
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        //Retrieves the fully-wired controller bean
        FlooringMasteryController controller = ctx.getBean("controller", FlooringMasteryController.class);

        //Allows the application to run inside the controller
        controller.run();
    }
}