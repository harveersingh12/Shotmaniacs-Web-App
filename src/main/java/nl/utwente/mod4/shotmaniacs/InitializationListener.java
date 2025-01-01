package nl.utwente.mod4.shotmaniacs;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import nl.utwente.mod4.shotmaniacs.dao.DatabaseConnection;

@WebListener
public class InitializationListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing Application...");
        System.out.println("Application initialized.");
        new DatabaseConnection();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Shutting down Application...");
        System.out.println("Application shutdown.");
    }
}
