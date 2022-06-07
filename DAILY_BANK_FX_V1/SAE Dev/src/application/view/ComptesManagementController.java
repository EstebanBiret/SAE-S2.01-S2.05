package application.view;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.ComptesManagement;
import application.control.ExceptionDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.orm.AccessCompteCourant;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.ManagementRuleViolation;
import model.orm.exception.RowNotFoundOrTooManyRowsException;

public class ComptesManagementController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private ComptesManagement cm;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private Client clientDesComptes;
	private ObservableList<CompteCourant> olCompteCourant;
	private ObservableList<CompteCourant> olCompteDesactive;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, ComptesManagement _cm, DailyBankState _dbstate, Client client) {
		this.cm = _cm;
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.clientDesComptes = client;
		this.configure();
	}
	
	/*
	 * Configuration de la fenêtre de la gestion des comptes
	 */
	private void configure() {
		String info;

		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.olCompteCourant = FXCollections.observableArrayList();
		this.olCompteDesactive = FXCollections.observableArrayList();
		this.lvComptes.setItems(this.olCompteCourant);
		this.lvComptes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvComptes.getFocusModel().focus(-1);
		this.lvComptes.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());

		info = this.clientDesComptes.nom + "  " + this.clientDesComptes.prenom + "  (id : "
				+ this.clientDesComptes.idNumCli + ")";
		this.lblInfosClient.setText(info);

		this.loadList();
		this.desaclist();
		this.validateComponentState();
		
		
	}

	public void displayDialog() {
		this.primaryStage.showAndWait();
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}
	
	@FXML
    private Button btnNouveauCompte;
	@FXML
	private Label lblInfosClient;
	@FXML
	private ListView<CompteCourant> lvComptes;
	@FXML
	private Button btnVoirOpes;
	@FXML
	private Button btnModifierCompte;
	@FXML
	private Button btnSupprCompte;
	
		
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.lvComptes.setItems(this.olCompteCourant);
	}
	
	/*
	 * Permet de fermer la fenêtre au clique d'un bouton
	 */
	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}
	
	/*
	 * Permet de voir les opérations d'un compte
	 */
	@FXML
	private void doVoirOperations() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			CompteCourant cpt = this.olCompteCourant.get(selectedIndice);
			this.cm.gererOperations(cpt);
		}
		this.loadList();
		this.validateComponentState();
	}
	
	
	/*
	 * Permet de modifier les informations d'un compte
	 */
	@FXML
	private void doModifierCompte() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			CompteCourant cpt = this.olCompteCourant.get(selectedIndice);
			this.cm.modifierCompte(cpt);
		}
		this.loadList();
		this.validateComponentState();
	}
	
	
	/*
	 * Permet de clôturer un compte
	 */
	@FXML
	private void doCloturerCompte() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0 && btnSupprCompte.getText().equals("Clôturer Compte")) {
			CompteCourant cptDesac = this.olCompteCourant.get(selectedIndice);

			Alert desac = new Alert(AlertType.CONFIRMATION);
			desac.setTitle("Cloturer un compte");
			desac.setContentText("Voulez-vous cloturer ce compte ?");
			desac.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

			Optional<ButtonType> reponse = desac.showAndWait();
			if (reponse.orElse(null) == ButtonType.YES) {
				AccessCompteCourant accpt = new AccessCompteCourant();
				try {
					accpt.closeCompteCourant(cptDesac);
				} catch (RowNotFoundOrTooManyRowsException | DataAccessException | DatabaseConnexionException
						| ManagementRuleViolation e) {
					ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
					ed.doExceptionDialog();
				}
			}
			desac.close();
		} else if (selectedIndice >= 0 && btnSupprCompte.getText().equals("Reactiver Compte")) {
			CompteCourant cptReac = this.olCompteCourant.get(selectedIndice);

			Alert reac = new Alert(AlertType.CONFIRMATION);
			reac.setTitle("Réactiver un compte");
			reac.setContentText("Voulez-vous réactiver ce compte ?");
			reac.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

			Optional<ButtonType> reponse = reac.showAndWait();
			if (reponse.orElse(null) == ButtonType.YES) {
				AccessCompteCourant accpt = new AccessCompteCourant();
				try {
					accpt.openagainCompteCourant(cptReac);
				} catch (RowNotFoundOrTooManyRowsException | DataAccessException | DatabaseConnexionException
						| ManagementRuleViolation e) {
					ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
					ed.doExceptionDialog();
				}
			}
			reac.close();
		}
		this.loadList();
		this.desaclist();
		this.validateComponentState();
	}
	/*
	 * Permet d'ajouter un nouveau compte
	 */
	@FXML
	private void doNouveauCompte() throws SQLException {
		CompteCourant compte;
		compte = this.cm.creerCompte();
		if (compte != null) {
			this.olCompteCourant.add(compte);
		}
	}
	/*
	 * Ajoute les comptes d'un client dans une liste
	 */
	public void loadList () {
		ArrayList<CompteCourant> listeCpt;
		listeCpt = this.cm.getComptesDunClient();
		this.olCompteCourant.clear();
		for (CompteCourant co : listeCpt) {
			this.olCompteCourant.add(co);
		}
	}
	
	/**
	 * Ajoute les comptes désactivés d'un client dans une liste
	 * (peut être useless)
	 */
	private void desaclist() {
		ArrayList<CompteCourant> listDesac;
		listDesac = this.cm.getComptesDunClient();
		this.olCompteDesactive.clear();
		for(CompteCourant co : listDesac) {
			if(co.estCloture.equals("O"))
				this.olCompteDesactive.add(co);
		}
	}
	
	/*
	 * Vérifie si les informations sont valide
	 */
	private void validateComponentState() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		CompteCourant cpt = this.lvComptes.getSelectionModel().getSelectedItem();
		if (selectedIndice >= 0 && cpt.estCloture.equals("O")) {
			this.btnVoirOpes.setDisable(true);
			this.btnModifierCompte.setDisable(true);
			this.btnSupprCompte.setDisable(false);
			this.btnSupprCompte.setText("Reactiver Compte");
		} else {
			this.btnVoirOpes.setDisable(false);
			this.btnModifierCompte.setDisable(false);
			this.btnSupprCompte.setDisable(false);
			this.btnSupprCompte.setText("Clôturer Compte");
		}
		
		if(this.clientDesComptes.estInactif.equals("O")) {
			this.btnSupprCompte.setDisable(true);
			this.btnNouveauCompte.setDisable(true);
		}
	}
}