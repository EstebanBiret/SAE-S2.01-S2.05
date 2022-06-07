package application.control;

import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.CategorieOperation;
import application.tools.PairsOfValue;
import application.tools.StageManagement;
import application.view.ComptesManagementController;
import application.view.OperationEditorPaneController;
import application.view.OperationsManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Operation;
import model.orm.AccessCompteCourant;
import model.orm.AccessOperation;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

/**
 * @author yann
 * classe dédié à la gestion de la fenêtre "gestion des opérations"
 */
public class OperationsManagement {

	/**
	 * Attributs
	 */
	
	private Stage primaryStage; //la fenêtre
	private DailyBankState dbs;
	private OperationsManagementController omc; //le controller de gestion des opérations
	private Client clientDuCompte; //un client ayant un compte
	private CompteCourant compteConcerne; //le compte du client
	private OperationEditorPaneController oepc;

	/**
	 * @param _parentStage
	 * @param _dbstate
	 * @param client
	 * @param compte
	 * représente la fenêtre "gestion des opérations" après avoir cliquer sur le bouton
	 * "voir opérations" des opérations d'un compte d'un client
	 */
	public OperationsManagement(Stage _parentStage, DailyBankState _dbstate, Client client, CompteCourant compte) {

		this.clientDuCompte = client;
		this.compteConcerne = compte;
		this.dbs = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(
					OperationsManagementController.class.getResource("operationsmanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, 900 + 20, 350 + 10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des opérations");
			this.primaryStage.setResizable(false);

			this.omc = loader.getController();
			this.omc.initContext(this.primaryStage, this, _dbstate, client, this.compteConcerne);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doOperationsManagementDialog() {
		this.omc.displayDialog();
	}

	/**
	 * @return une opération lorsqu'on effectue un débit sur un compte
	 */
	public Operation enregistrerDebit() {

		OperationEditorPane oep = new OperationEditorPane(this.primaryStage, this.dbs);
		Operation op = oep.doOperationEditorDialog(this.compteConcerne, CategorieOperation.DEBIT);
		if (op != null) {
			try {
				AccessOperation ao = new AccessOperation();
				
				//si l'employé est chef d'agence, il peut faire un débit exceptionnel
				if(this.dbs.isChefDAgence()) {
					ao.insertDebitEx(this.compteConcerne.idNumCompte, op.montant, op.idTypeOp);
				}
				//sinon il ne peut faire qu'un débit standard (il reçoit donc une exception)
				else {
					ao.insertDebit(this.compteConcerne.idNumCompte, op.montant, op.idTypeOp);
				}

			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				op = null;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				op = null;
			}
		}
		return op;
	}
	
	/**
	 * @return une opération lorsqu'on effectue un crédit sur un compte
	 */
	public Operation enregistrerCredit() {

		OperationEditorPane oep = new OperationEditorPane(this.primaryStage, this.dbs);
		Operation op = oep.doOperationEditorDialog(this.compteConcerne, CategorieOperation.CREDIT);
		if (op != null) {
			try {
				AccessOperation ao = new AccessOperation();

				ao.insertCredit(this.compteConcerne.idNumCompte, op.montant, op.idTypeOp);

			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				op = null;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				op = null;
			}
		}
		return op;
	}
	
	/**
	 * @return une opération lorsqu'on effectue un virement de compte à compte
	 */
	public Operation enregistrerVirement() {
		
		int indiceErreur = 0;
		ArrayList<CompteCourant> numCompte = new ArrayList<CompteCourant>();
		OperationEditorPane oep = new OperationEditorPane(this.primaryStage, this.dbs);
		Operation op = oep.doOperationEditorDialog(this.compteConcerne, CategorieOperation.VIREMENT);
		if (op != null) {
			try {
				AccessCompteCourant acc = new AccessCompteCourant();
				numCompte = acc.getCompteCourants(this.compteConcerne.idNumCli);
				AccessOperation ao = new AccessOperation();
				for(int i=0;i<numCompte.size();i++) {
					if(oep.getOepc().getId() == numCompte.get(i).idNumCompte) {
						CompteCourant compteDeux = acc.getCompteCourant(numCompte.get(i).idNumCompte);
						ao.insertDebit(this.compteConcerne.idNumCompte, op.montant, op.idTypeOp);
						ao.insertCredit(compteDeux.idNumCompte, op.montant, op.idTypeOp);
						indiceErreur = 1;
					}
				}
				if(indiceErreur == 0) {
					Alert dialog = new Alert(AlertType.INFORMATION);
					dialog.setTitle("Erreur numéro de compte");
					dialog.setHeaderText("Impossible de faire un virement vers un compte qui ne vous appartient pas !");
					dialog.showAndWait();
				}
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				op = null;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				op = null;
			}
		}
		return op;
	}
	

	public PairsOfValue<CompteCourant, ArrayList<Operation>>  operationsEtSoldeDunCompte() {
		ArrayList<Operation> listeOP = new ArrayList<>();

		try {
			// Relecture BD du solde du compte
			AccessCompteCourant acc = new AccessCompteCourant();
			this.compteConcerne = acc.getCompteCourant(this.compteConcerne.idNumCompte);

			// lecture BD de la liste des opérations du compte de l'utilisateur
			AccessOperation ao = new AccessOperation();
			listeOP = ao.getOperations(this.compteConcerne.idNumCompte);

		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeOP = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
			listeOP = new ArrayList<>();
		}
		System.out.println(this.compteConcerne.solde);
		return new PairsOfValue<>(this.compteConcerne, listeOP);
	}
}