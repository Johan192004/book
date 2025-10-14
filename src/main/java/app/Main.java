package app;

import config.DatabaseConfig;
import controller.AuthController;
import controller.CatalogController;
import controller.MemberController;
import dao.impl.CatalogDaoImpl;
import dao.impl.MemberDaoImpl;
import dao.impl.UserDaoImpl;
import service.AuthService;
import service.CatalogService;
import service.MemberService;
import view.CatalogView;
import view.MainView;
import view.MemberView;
import dao.CatalogDao;
import dao.MemberDao;
import dao.UserDao;

public class Main {
    public static void main(String[] args) {
        DatabaseConfig dbConfig = new DatabaseConfig();

        UserDao userDao = new UserDaoImpl(dbConfig.getInstance());
        MemberDao memberDao = new MemberDaoImpl(dbConfig.getInstance());
        CatalogDao catalogDao = new CatalogDaoImpl(dbConfig.getInstance());

        AuthService authService = new AuthService(userDao);
        MemberService memberService = new MemberService(memberDao);
        CatalogService catalogService = new CatalogService(catalogDao);

        MemberController memberController = new MemberController(memberService);
        AuthController authController = new AuthController(authService);
        CatalogController catalogController = new CatalogController(catalogService);

        MemberView memberView = new MemberView(memberController);
        CatalogView catalogView = new CatalogView(catalogController);

        MainView mainView = new MainView(authController, memberView, catalogView);
        mainView.showMenu();

        dbConfig.closeConnection();
    }
}
