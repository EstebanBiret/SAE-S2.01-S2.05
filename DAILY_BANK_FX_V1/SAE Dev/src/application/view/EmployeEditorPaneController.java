	package application.view;

import java.net.URL;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.ConstantesIHM;
import application.tools.EditionMode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Employe;

public class EmployeEditorPaneController implements Initializable {

	// Etat application
	private DailyBankState dbs;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private Employe employeEdite;
	private EditionMode em;
	private Employe employeResult;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, DailyBankState _dbstate) {
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}
	

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}
	
	/*
	 * Configuration de la fenêtre d'ajout/modification/suppression d'un employe
	 * @param in employe : L'employé à ajouter
	 * @param in mode : mode d'édition utiliser
	 *
	 */
	public Employe displayDialog(Employe employe, EditionMode mode) {

		this.em = mode;
		if (employe == null) {
			this.employeEdite = new Employe(0, "", "", "", "login", "password", this.dbs.getEmpAct().idAg);
		} else {
			this.employeEdite = new Employe(employe);
		}
		this.employeResult = null;
		switch (mode) {
		case CREATION:
			this.txtIdemp.setDisable(true);
			this.txtNom.setDisable(false);
			this.txtPrenom.setDisable(false);
			
			this.txtLogin.setDisable(false);
			this.txtMDP.setDisable(false);
			this.lblMessage.setText("Informations sur le nouvel employé");
			this.butOk.setText("Ajouter");
			this.butCancel.setText("Annuler");
			break;
		case MODIFICATION:
			this.txtIdemp.setDisable(true);
			this.txtNom.setDisable(false);
			this.txtPrenom.setDisable(false);
			
			this.txtLogin.setDisable(false);
			this.txtMDP.setDisable(false);
			this.lblMessage.setText("Informations employé");
			this.butOk.setText("Modifier");
			this.butCancel.setText("Annuler");
			break;
		case SUPPRESSION:
			this.txtIdemp.setDisable(true);
			this.txtNom.setDisable(true);
			this.txtPrenom.setDisable(true);
			this.rbChefAgence.setDisable(true);
			this.rbGuichetier.setDisable(true);
			
			this.lblMessage.setText("Réactivation employé");
			this.butOk.setText("Réactiver");
			
			
			if(employe.droitsAccess == "guichetier") {
				this.rbGuichetier.setSelected(true);
			}
			
			if(employe.droitsAccess == "ChefAgence") {
				this.rbChefAgence.setSelected(true);
			}
			
			break;
		}
		// Paramétrages spécifiques pour les chefs d'agencesx
		if (ConstantesIHM.isAdmin(this.dbs.getEmpAct())) {
			// rien pour l'instant
		}
		// initialisation du contenu des champs
		this.txtIdemp.setText("" + this.employeEdite.idEmploye);
		this.txtNom.setText(this.employeEdite.nom);
		this.txtPrenom.setText(this.employeEdite.prenom);
		
		this.txtLogin.setText(this.employeEdite.login);
		this.txtMDP.setText(this.employeEdite.motPasse);


		this.employeResult = null;

		this.primaryStage.showAndWait();
		return this.employeResult;
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions
	@FXML
	private Label lblMessage;
	@FXML
	private TextField txtIdemp;
	@FXML
	private TextField txtNom;
	@FXML
	private TextField txtPrenom;
	@FXML
    private RadioButton rbGuichetier;

    @FXML
    private ToggleGroup guichetierChefAgence;

    @FXML
    private RadioButton rbChefAgence;
	@FXML
	private TextField txtLogin;
	@FXML
	private TextField txtMDP;
	@FXML
	private Button butOk;
	@FXML
	private Button butCancel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	/*
	 * Permet de fermer la fenêtre au clique d'un bouton
	 */
	@FXML
	private void doCancel() {
		this.employeResult = null;
		this.primaryStage.close();
	}
	
	/*
	 * Permet de confirmer les informations au clique d'un bouton
	 */
	@FXML
	private void doAjouter() {
		switch (this.em) {
		case CREATION:
			if (this.isSaisieValide()) {
				this.employeResult = this.employeEdite;
				if(this.rbGuichetier.isSelected()) {
					this.employeResult.droitsAccess = "guichetier";
				} else {
					this.employeResult.droitsAccess = "chefAgence";
				}
				this.primaryStage.close();
			}
			break;
		case MODIFICATION:
			if (this.isSaisieValide()) {
				this.employeResult = this.employeEdite;
				if(this.rbGuichetier.isSelected()) {
					this.employeResult.droitsAccess = "guichetier";
				} else {
					this.employeResult.droitsAccess = "chefAgence";
				}
				this.primaryStage.close();
			}
			break;
		case SUPPRESSION:
			this.employeResult = this.employeEdite;
			this.primaryStage.close();
			break;
		}

	}
	
	/*
	 * Permet de vérifiez si la saisie des différent champs est correct
	 * renvoie une alerte si :
	 * - le nom est vide
	 * - le prénom est vide
	 * - le login n'est pas valide
	 * - le mot de passe n'est pas valide
	 */
	private boolean isSaisieValide() {
		this.employeEdite.nom = this.txtNom.getText().trim();
		this.employeEdite.prenom = this.txtPrenom.getText().trim();
		this.employeEdite.login = this.txtLogin.getText().trim();
		this.employeEdite.motPasse = this.txtMDP.getText().trim();

		if (this.employeEdite.nom.isEmpty()) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le nom ne doit pas être vide",
					AlertType.WARNING);
			this.txtNom.requestFocus();
			return false;
		}
		if (this.employeEdite.prenom.isEmpty()) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le prénom ne doit pas être vide",
					AlertType.WARNING);
			this.txtPrenom.requestFocus();
			return false;
		}
		
		if (this.employeEdite.login.isEmpty()) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le login ne doit pas être vide",
					AlertType.WARNING);
			this.txtPrenom.requestFocus();
			return false;
		}
		
		if (this.employeEdite.motPasse.isEmpty()) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le mot de passe ne doit pas être vide",
					AlertType.WARNING);
			this.txtPrenom.requestFocus();
			return false;
		}


		return true;
	}
}