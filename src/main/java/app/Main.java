package app;

import config.DatabaseConfig;
import controller.AuthController;
import controller.MemberController;
import dao.impl.MemberDaoImpl;
import dao.impl.UserDaoImpl;
import service.AuthService;
import service.MemberService;
import view.MainView;
import view.MemberView;
import dao.MemberDao;
import dao.UserDao;

public class Main {
    public static void main(String[] args) {
        DatabaseConfig dbConfig = new DatabaseConfig();

        UserDao userDao = new UserDaoImpl(dbConfig.getInstance());
        MemberDao memberDao = new MemberDaoImpl(dbConfig.getInstance());

        AuthService authService = new AuthService(userDao);
        MemberService memberService = new MemberService(memberDao);

        MemberController memberController = new MemberController(memberService);
        AuthController authController = new AuthController(authService);

        MemberView memberView = new MemberView(memberController);

        MainView mainView = new MainView(authController, memberView);
        mainView.showMenu();
    }
}
