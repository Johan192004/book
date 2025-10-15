package app;

import config.DatabaseConfig;
import controller.AuthController;
import controller.CatalogController;
import controller.LoanController;
import controller.MemberController;
import controller.UserController;
import dao.impl.CatalogDaoImpl;
import dao.impl.LoanDaoImpl;
import dao.impl.MemberDaoImpl;
import dao.impl.UserDaoImpl;
import service.AuthService;
import service.CatalogService;
import service.LoanService;
import service.MemberService;
import service.UserService;
import view.CatalogView;
import view.LoanView;
import view.MainView;
import view.MemberView;
import view.UserView;
import dao.CatalogDao;
import dao.LoanDao;
import dao.MemberDao;
import dao.UserDao;

public class Main {
    public static void main(String[] args) {
        DatabaseConfig dbConfig = new DatabaseConfig();

        UserDao userDao = new UserDaoImpl(dbConfig.getInstance());
        MemberDao memberDao = new MemberDaoImpl(dbConfig.getInstance());
        CatalogDao catalogDao = new CatalogDaoImpl(dbConfig.getInstance());
        LoanDao loanDao = new LoanDaoImpl(dbConfig.getInstance());

        AuthService authService = new AuthService(userDao);
        MemberService memberService = new MemberService(memberDao, dbConfig.getInstance());
        CatalogService catalogService = new CatalogService(catalogDao, dbConfig.getInstance());
        UserService userService = new UserService(userDao, dbConfig.getInstance());
        LoanService loanService = new LoanService(loanDao, memberDao, catalogDao, dbConfig.getInstance());

        MemberController memberController = new MemberController(memberService);
        AuthController authController = new AuthController(authService);
        CatalogController catalogController = new CatalogController(catalogService);
        UserController userController = new UserController(userService);
        LoanController loanController = new LoanController(loanService);

        MemberView memberView = new MemberView(memberController);
        CatalogView catalogView = new CatalogView(catalogController);
        UserView userView = new UserView(userController);
        LoanView loanView = new LoanView(loanController);

        MainView mainView = new MainView(authController, memberView, catalogView, userView, loanView);
        mainView.showMenu();

        dbConfig.closeConnection();
    }
}
