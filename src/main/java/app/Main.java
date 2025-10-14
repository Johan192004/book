package app;

import config.DatabaseConfig;
import controller.AuthController;
import dao.impl.UserDaoImpl;
import service.AuthService;
import view.MainView;
import dao.UserDao;

public class Main {
    public static void main(String[] args) {
        DatabaseConfig dbConfig = new DatabaseConfig();

        UserDao userDao = new UserDaoImpl(dbConfig.getInstance());

        AuthService authService = new AuthService(userDao);

        AuthController authController = new AuthController(authService);

        MainView mainView = new MainView(authController);
        mainView.showMenu();
    }
}
