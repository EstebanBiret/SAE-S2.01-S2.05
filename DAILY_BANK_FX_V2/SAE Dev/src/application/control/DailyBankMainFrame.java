package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.view.DailyBankMainFrameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.data.AgenceBancaire;
import model.orm.AccessAgenceBancaire;
import model.orm.LogToDatabase;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

public class DailyBankMainFrame extends Application {

	/**
	 * Attributs
	 */
	
	private DailyBankState dbs;
	private Stage primaryStage; //la fenêtre principale

	@Override
	public void start(Stage primaryStage) {

		this.primaryStage = primaryStage;

		try {
			this.dbs = new DailyBankState();
			this.dbs.setAgAct(null);
			this.dbs.setChefDAgence(false);
			this.dbs.setEmpAct(null);

			FXMLLoader loader = new FXMLLoader(
					DailyBankMainFrameController.class.getResource("dailybankmainframe.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+20, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			primaryStage.setScene(scene);
			primaryStage.setTitle("Fenêtre Principale");

			DailyBankMainFrameController dbmc = loader.getController();
			dbmc.initContext(primaryStage, this, this.dbs);

			dbmc.displayDialog();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * méthode qui permet de lancer l'application
	 */
	public static void runApp() {
		Application.launch();
	}

	public void disconnect() {
		this.dbs.setAgAct(null);
		this.dbs.setEmpAct(null);
		this.dbs.setChefDAgence(false);
		try {
			LogToDatabase.closeConnexion();
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
		}
	}

	/**
	 * méthode qui gère l'exécution de code lorsqu'on clique sur le bouton "connexion"
	 */
	public void login() {
		LoginDialog ld = new LoginDialog(this.primaryStage, this.dbs);
		ld.doLoginDialog();

		if (this.dbs.getEmpAct() != null) {
			this.dbs.setChefDAgence(this.dbs.getEmpAct().droitsAccess);
			try {
				AccessAgenceBancaire aab = new AccessAgenceBancaire();
				AgenceBancaire agTrouvee;

				agTrouvee = aab.getAgenceBancaire(this.dbs.getEmpAct().idAg);
				this.dbs.setAgAct(agTrouvee);
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.dbs.setAgAct(null);
				this.dbs.setEmpAct(null);
				this.dbs.setChefDAgence(false);
			} catch (ApplicationException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.dbs.setAgAct(null);
				this.dbs.setEmpAct(null);
				this.dbs.setChefDAgence(false);
			}
		}
	}

	/**
	 * méthode qui gère l'exécution de code lorsqu'on clique sur le bouton "gestion > clients"
	 */
	public void gestionClients() {
		ClientsManagement cm = new ClientsManagement(this.primaryStage, this.dbs);
		cm.doClientManagementDialog();
	}
	
	/**
	 * méthode qui gère l'exécution de code lorsqu'on clique sur le bouton "gestion > employés"
	 */
	public void gestionEmploye() {
		EmployeManagement em = new EmployeManagement(this.primaryStage, this.dbs);
		em.doEmployeManagementDialog();
	}
}