package application.control;

import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.ClientsManagementController;
import application.view.EmployeManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.Employe;
import model.orm.AccessClient;
import model.orm.AccessEmploye;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

public class EmployeManagement {
	

	/**
	 * attributs
	 */
	
	private Stage primaryStage; //fenêtre principale
	private DailyBankState dbs;
	private EmployeManagementController cmc; //le controller relié au management d'un client

	/**
	 * @param _parentStage
	 * @param _dbstate
	 * représente la fenêtre de l'application "gestion d"un client" lorsque l'on s'est connecté
	 */
	public EmployeManagement(Stage _parentStage, DailyBankState _dbstate) {
		this.dbs = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(EmployeManagementController.class.getResource("employemanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+50, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des employés");
			this.primaryStage.setResizable(false);

			this.cmc = loader.getController();
			this.cmc.initContext(this.primaryStage, this, _dbstate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doEmployeManagementDialog() {
		this.cmc.displayDialog();
	}

	/**
	 * @param c l'employé à modifier
	 * @return l'employé avec ses informations modifiées
	 */
	public Employe modifierEmploye(Employe c) {
		EmployeEditorPane cep = new EmployeEditorPane(this.primaryStage, this.dbs);
		Employe result = cep.doEmployeEditorDialog(c, EditionMode.MODIFICATION);
		if (result != null) {
			try {
				AccessEmploye ac = new AccessEmploye();
				ac.updateEmploye(result);
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				result = null;
				this.primaryStage.close();
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				result = null;
			}
		}
		return result;
	}

	/**
	 * Permet de créer un employé
	 * @return un nouvel employé avec ses informations de nom, prénom, droit d'accès, login et mot de passe
	 */
	public Employe nouveauEmploye() {
		Employe employe;
		EmployeEditorPane cep = new EmployeEditorPane(this.primaryStage, this.dbs);
		employe = cep.doEmployeEditorDialog(null, EditionMode.CREATION);
		if (employe != null) {
			try {
				AccessEmploye ac = new AccessEmploye();

				ac.insertEmploye(employe);
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				employe = null;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				employe = null;
			}
		}
		return employe;
	}
	
	
	/*public Employe reactiverEmploye(Employe c) {
		EmployeEditorPane cep = new EmployeEditorPane(this.primaryStage, this.dbs);
		Employe result = cep.doEmployeEditorDialog(c, EditionMode.SUPPRESSION);
		if (result != null) {
			try {
				AccessEmploye ac = new AccessEmploye();
				ac.reacEmploye(result);
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				result = null;
				this.primaryStage.close();
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				result = null;
			}
		}
		return result;
	}*/

	

	/**
	 * @param _numCompte
	 * @param _debutNom
	 * @param _debutPrenom
	 * @return une liste d'employé en fonction de plusieurs spécifications
	 */
	public ArrayList<Employe> getlisteEmploye(int _numCompte, String _login, String _mdp) {
		ArrayList<Employe> listeEmp = new ArrayList<>();
		try {
			// Recherche des employés en BD. cf. AccessEmploye > getEmploye(.)
			// numCompte != -1 => recherche sur numCompte
			// numCompte != -1 et debutNom non vide => recherche nom/prenom
			// numCompte != -1 et debutNom vide => recherche tous les clients

			AccessEmploye ac = new AccessEmploye();
			listeEmp = ac.getEmployes(this.dbs.getEmpAct().idAg, _numCompte, _login, _mdp);

		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeEmp = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
			listeEmp = new ArrayList<>();
		}
		return listeEmp;
	}
}