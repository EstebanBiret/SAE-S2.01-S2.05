package application.view;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.ConstantesIHM;
import application.tools.EditionMode;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.orm.AccessCompteCourant;
import model.orm.LogToDatabase;
import model.orm.exception.DatabaseConnexionException;

public class CompteEditorPaneController implements Initializable {

	// Etat application
	private DailyBankState dbs;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private EditionMode em;
	private Client clientDuCompte;
	private CompteCourant compteEdite;
	private CompteCourant compteResult;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, DailyBankState _dbstate) {
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.txtDecAutorise.focusedProperty().addListener((t, o, n) -> this.focusDecouvert(t, o, n));
		this.txtSolde.focusedProperty().addListener((t, o, n) -> this.focusSolde(t, o, n));
	}

	/*
	 * Configuration de la fenêtre d'édition d'un compte
	 * @param in client : le client
	 * @param in cpte : le client
	 * @param in mode : le mode de modification
	 * return le resultat
	 */
	public CompteCourant displayDialog(Client client, CompteCourant cpte, EditionMode mode) {
		this.clientDuCompte = client;
		this.em = mode;
		if (cpte == null) {
			this.compteEdite = new CompteCourant(0, 200, 0, "N", this.clientDuCompte.idNumCli);

		} else {
			this.compteEdite = new CompteCourant(cpte);
		}
		this.compteResult = null;
		this.txtIdclient.setDisable(true);
		this.txtIdAgence.setDisable(true);
		this.txtIdNumCompte.setDisable(true);
		switch (mode) {
		case CREATION:
			this.txtDecAutorise.setDisable(false);
			this.txtSolde.setDisable(false);
			this.lblMessage.setText("Informations sur le nouveau compte");
			this.lblSolde.setText("Solde (premier dépôt)");
			this.btnOk.setText("Ajouter");
			this.btnCancel.setText("Annuler");
			break;
		case MODIFICATION:
			this.txtDecAutorise.setDisable(false);
			this.txtSolde.setDisable(true);
			this.lblMessage.setText("Modification du compte");
			this.btnOk.setText("Modifier");
			this.btnCancel.setText("Annuler");
			break;
		case SUPPRESSION:
			//break;
		}

		// Paramétrages spécifiques pour les chefs d'agences
		if (ConstantesIHM.isAdmin(this.dbs.getEmpAct())) {
			// rien pour l'instant
		}

		// initialisation du contenu des champs
		this.txtIdclient.setText("" + this.compteEdite.idNumCli);
		this.txtIdNumCompte.setText("" + this.compteEdite.idNumCompte);
		this.txtIdAgence.setText("" + this.dbs.getEmpAct().idAg);
		this.txtDecAutorise.setText("" + this.compteEdite.debitAutorise);
		this.txtSolde.setText(String.format(Locale.ENGLISH, "%10.02f", this.compteEdite.solde));

		this.compteResult = null;

		this.primaryStage.showAndWait();
		return this.compteResult;
	}

	/*
	 * Ajoute un compte à la base de donnée
	 */
	public CompteCourant creerCompte() {
		AccessCompteCourant ac = new AccessCompteCourant();

		CompteCourant compte = this.compteEdite; // compte courant

		if (compte != null) {
			//ac.insertCompte(compte);
			System.out.println("oui oui baguette");
		}

		return compte;
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	/*
	 * 
	 */
	private Object focusDecouvert(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
			boolean newPropertyValue) {
		if (oldPropertyValue) {
			try {
				int val;
				val = Integer.parseInt(this.txtDecAutorise.getText().trim());
				val *= -1;
				//				if (val < 0) {
				//					throw new NumberFormatException();
				//				}
				if (val > 0) {
					throw new NumberFormatException();
				}
				this.compteEdite.debitAutorise = val;
			} catch (NumberFormatException nfe) {
				this.txtDecAutorise.setText("" + this.compteEdite.debitAutorise);
			}
		}
		return null;
	}

	/*
	 * 
	 */
	private Object focusSolde(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
			boolean newPropertyValue) {
		if (oldPropertyValue) {
			try {
				double val;
				val = Double.parseDouble(this.txtSolde.getText().trim());
				if (val < 0) {
					throw new NumberFormatException();
				}
				this.compteEdite.solde = val;
			} catch (NumberFormatException nfe) {
				this.txtSolde.setText(String.format(Locale.ENGLISH, "%10.02f", this.compteEdite.solde));
			}
		}
		this.txtSolde.setText(String.format(Locale.ENGLISH, "%10.02f", this.compteEdite.solde));
		return null;
	}

	// Attributs de la scene + actions
	@FXML
	private Label lblMessage;
	@FXML
	private Label lblSolde;
	@FXML
	private TextField txtIdclient;
	@FXML
	private TextField txtIdAgence;
	@FXML
	private TextField txtIdNumCompte;
	@FXML
	private TextField txtDecAutorise;
	@FXML
	private TextField txtSolde;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	/*
	 * Permet de fermer une fenêtre au clique d'un bouton 
	 */
	@FXML
	private void doCancel() {
		this.compteResult = null;
		this.primaryStage.close();
	}

	/*
	 * Permet d'ajouter un compte au clique d'un bouton
	 */
	@FXML
	private void doAjouter() throws DatabaseConnexionException, SQLException {
		switch (this.em) {
		case CREATION:
			/*
			// Utilisation de la méthode créer compte

			this.creerCompte();
			this.primaryStage.close();



			////////////////////////////////////////////////////////////
			NE PAS EFFACER : UTILISER POUR LA FONCTIONNALITE CREERCOMPTE
			////////////////////////////////////////////////////////////

			 requete sql :
			 insert into comptecourant 
			 values (12, 0, 350, 5, 'N'); 


			int idNumCompte, idNumClient;
			double debitAuto, solde;
			String cloture;

			idNumCompte = Integer.parseInt(this.txtIdNumCompte.getText());
			debitAuto = Double.parseDouble(this.txtDecAutorise.getText());
			solde = Double.parseDouble(this.txtSolde.getText());
			idNumClient = Integer.parseInt(this.txtIdNumCompte.getText());
			cloture = this.compteEdite.estCloture;



				Connection con = LogToDatabase.getConnexion();

				String query = "INSERT INTO COMPTECOURANT";
				query += " VALUES(seq_IDNUMCOMPTE.NEXTVAL,?,?,?,?)";

				String query = "INSERT INTO COMPTECOURANT VALUES (" + "seq_id_client.NEXTVAL" + ", " + "?" + ", " + "?" + ", " + "?" + ", " + "?" + ")";

				try {
					PreparedStatement pst = con.prepareStatement(query);
					pst.setDouble(1, debitAuto);
					pst.setDouble(2, solde);
					pst.setInt(3, idNumClient);
					pst.setString(4, cloture);

					ResultSet rs = pst.executeQuery();
					pst.close();
				} catch (Exception e ) {
					e.printStackTrace();
				}
			 */
			if (this.isSaisieValide()) {
				this.compteResult = this.compteEdite;
				this.primaryStage.close();
			}


			break;

		case MODIFICATION:
			if (this.isSaisieValide()) {
				this.compteResult = this.compteEdite;
				this.primaryStage.close();

			}
			break;
		case SUPPRESSION:
			this.compteResult = this.compteEdite;
			this.primaryStage.close();

			break;
		}

	}

	/*
	 * Permet de vérifiez si les saisies sont valides : 
	 * renvoie une alerte si :
	 * - id du client n'est pas valide 
	 * - id de l'agence n'est pas valide
	 * - le numéro de compte n'est pas valide
	 * - le montant du découvert est négatif
	 * - le montant du solde (du premier dépôt est négatif)
	 */
	private boolean isSaisieValide() {

		return true;
	}
}