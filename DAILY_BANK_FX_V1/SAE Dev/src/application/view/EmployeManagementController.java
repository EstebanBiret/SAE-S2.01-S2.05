package application.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.ClientsManagement;
import application.control.EmployeManagement;
import application.control.ExceptionDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Employe;
import model.orm.AccessEmploye;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.RowNotFoundOrTooManyRowsException;

public class EmployeManagementController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private EmployeManagement em;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private ObservableList<Employe> olc;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, EmployeManagement _em, DailyBankState _dbstate) {
		this.em = _em;
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.olc = FXCollections.observableArrayList();
		this.lvEmploye.setItems(this.olc);
		this.lvEmploye.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvEmploye.getFocusModel().focus(-1);
		this.lvEmploye.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
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

	// Attributs de la scene + actions
	@FXML
	private TextField txtNum;
	@FXML
	private TextField txtLogin;
	@FXML
	private TextField txtMDP;
	@FXML
	private ListView<Employe> lvEmploye;
	@FXML
	private Button btnSuppEmploye;
	@FXML
	private Button btnModifEmploye;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	/*
	 * Permet de fermer la fenêtre au clique d'un bouton
	 */
	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	/*
	 * Permet de rechercher un employé avec des informations spécifiques
	 * si les informations ne sont pas présentes, elle seront vides dans le ListView
	 */
	@FXML
	private void doRechercher() {
		int idEmp;
		try {
			String nc = this.txtNum.getText();
			if (nc.equals("")) {
				idEmp = -1;
			} else {
				idEmp = Integer.parseInt(nc);
				if (idEmp < 0) {
					this.txtNum.setText("");
					idEmp = -1;
				}
			}
		} catch (NumberFormatException nfe) {
			this.txtNum.setText("");
			idEmp = -1;
		}

		String login = this.txtLogin.getText();
		String mdp = this.txtMDP.getText();

		if (idEmp != -1) {
			this.txtMDP.setText("");
		}

		// Recherche des employés en BD. cf. AccessEmploye > getEmploye(.)
		// numCompte != -1 => recherche sur numCompte
		// numCompte != -1 et debutNom non vide => recherche nom/prenom
		// numCompte != -1 et debutNom vide => recherche tous les clients
		ArrayList<Employe> listeEmp;
		listeEmp = this.em.getlisteEmploye(idEmp, login, mdp);

		this.olc.clear();
		for (Employe cli : listeEmp) {
			this.olc.add(cli);
		}

		this.validateComponentState();
	}

	/*
	 * Permet de modifier les informations d'un employé
	 */
	@FXML
	private void doModifierEmploye() {

		int selectedIndice = this.lvEmploye.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Employe empMod = this.olc.get(selectedIndice);
			Employe result = this.em.modifierEmploye(empMod);
			if (result != null) {
				this.olc.set(selectedIndice, result);
			}
		}
	}

	/*
	 * Permet de désactiver (supprimer) un employé
	 */
	@FXML
	private void doSuppEmploye() {
		int selectedIndice = this.lvEmploye.getSelectionModel().getSelectedIndex();

		Employe empDesac = this.olc.get(selectedIndice);
		AccessEmploye ac = new AccessEmploye();
		System.out.println(empDesac.droitsAccess);

		/*if (empDesac.login == "") {
			Alert alerte = new Alert(AlertType.CONFIRMATION);
			alerte.setTitle("Réactiver l'employé");
			alerte.setHeaderText("Voulez-vous réactiver l'employé ?");
			alerte.showAndWait().ifPresent(response -> {
				if (response == ButtonType.OK) {
					int Indice = this.lvEmploye.getSelectionModel().getSelectedIndex();
					if (Indice >= 0) {
						Employe emp = this.olc.get(selectedIndice);
						Employe result = this.em.reactiverEmploye(emp);
						if (result != null) {
							this.olc.set(selectedIndice, result);
						}
					}


					/*try {
						ac.reacEmploye(empDesac);
						System.out.println(empDesac.toString());
					} catch (RowNotFoundOrTooManyRowsException | DataAccessException | DatabaseConnexionException e) {
						ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
						ed.doExceptionDialog();

					}*/
		/*
			});
		}*/

		//else {
		Alert alerte = new Alert(AlertType.CONFIRMATION);
		alerte = new Alert(AlertType.CONFIRMATION);
		alerte.setTitle("Désactivation de l'employé");
		alerte.setHeaderText("Voulez-vous désactiver l'employé ?");
		alerte.setContentText("Possibilité de consulter ses informations, mais plus de se connecter avec.");
		alerte.showAndWait().ifPresent(response -> {
			if (response == ButtonType.OK) {
				try {
					ac.supprEmploye(empDesac);
					System.out.println(empDesac.toString());
				} catch (RowNotFoundOrTooManyRowsException | DataAccessException | DatabaseConnexionException e) {
					ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
					ed.doExceptionDialog();
				}
			}
		});

		this.doRechercher();

	}



	/*
	 * Permet d'ajouter un nouveau employé
	 */
	@FXML
	private void doNouveauEmploye() {
		Employe employe;
		employe = this.em.nouveauEmploye();
		if (employe != null) {
			this.olc.add(employe);
		}
	}

	/*
	 * Permet de désactiver certains boutons
	 */
	private void validateComponentState() {
		int selectedIndice = this.lvEmploye.getSelectionModel().getSelectedIndex();
		Employe emp = this.lvEmploye.getSelectionModel().getSelectedItem();
		if (selectedIndice >= 0 && emp.login != "") {
			this.btnModifEmploye.setDisable(false);
			this.btnSuppEmploye.setDisable(false);
			this.btnSuppEmploye.setText("Désactiver employé");
		} else {
			this.btnModifEmploye.setDisable(true);
			this.btnSuppEmploye.setDisable(true);

		}

		if(selectedIndice < 0) {
			this.btnSuppEmploye.setDisable(true);
		}



	}
}