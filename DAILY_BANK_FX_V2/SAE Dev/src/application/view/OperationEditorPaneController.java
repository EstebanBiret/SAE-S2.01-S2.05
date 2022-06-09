package application.view;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.tools.CategorieOperation;
import application.tools.ConstantesIHM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.CompteCourant;
import model.data.Operation;

public class OperationEditorPaneController implements Initializable {

	// Etat application
	private DailyBankState dbs;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private CategorieOperation categorieOperation;
	private CompteCourant compteEdite;
	private Operation operationResultat;
	
	private boolean indiceAdmin = false;//indice signifiant si l'employé est chef d'agence ou non (true si oui)


	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, DailyBankState _dbstate) {
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}

	public Operation displayDialog(CompteCourant cpte, CategorieOperation mode) {
		this.categorieOperation = mode;
		this.compteEdite = cpte;

		switch (mode) {
		case DEBIT:

			String info = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info);

			this.btnOk.setText("Effectuer Débit");
			this.btnCancel.setText("Annuler débit");

			ObservableList<String> list = FXCollections.observableArrayList();

			for (String tyOp : ConstantesIHM.OPERATIONS_DEBIT_GUICHET) {
				list.add(tyOp);
			}

			this.cbTypeOpe.setItems(list);
			this.cbTypeOpe.getSelectionModel().select(0);
			break;
		case CREDIT:
			
			String infoCredit = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(infoCredit);

			this.btnOk.setText("Effectuer Crédit");
			this.btnCancel.setText("Annuler Crédit");

			ObservableList<String> listCredit = FXCollections.observableArrayList();

			for (String tyOp : ConstantesIHM.OPERATIONS_CREDIT_GUICHET) {
				listCredit.add(tyOp);
			}

			this.cbTypeOpe.setItems(listCredit);
			this.cbTypeOpe.getSelectionModel().select(0);
			break;
		case VIREMENT:
			this.txtNumCompte.setVisible(true);
			this.lblNumCompte.setVisible(true);
			String infoV = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(infoV);

			
			this.btnOk.setText("Effectuer Virement");
			this.btnCancel.setText("Annuler Virement");

			ObservableList<String> listVirement = FXCollections.observableArrayList();

			for (String tyOp : ConstantesIHM.OPERATIONS_VIREMENT_GUICHET) {
				listVirement.add(tyOp);
			}

			this.cbTypeOpe.setItems(listVirement);
			this.cbTypeOpe.getSelectionModel().select(0);
			break;
		}
		// Paramétrages spécifiques pour les chefs d'agences
		if (ConstantesIHM.isAdmin(this.dbs.getEmpAct())) {

		}
			if (ConstantesIHM.isAdmin(this.dbs.getEmpAct())) {
					indiceAdmin = true;
			}

		this.operationResultat = null;
		this.cbTypeOpe.requestFocus();

		this.primaryStage.showAndWait();
		return this.operationResultat;
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}
	
	public int getId() {
		return Integer.parseInt(this.txtNumCompte.getText());
	}

	// Attributs de la scene + actions
	@FXML
	private Label lblMessage;
	@FXML
	private Label lblMontant;
	@FXML
	private Label lblNumCompte;
	@FXML
	private ComboBox<String> cbTypeOpe;
	@FXML
	private TextField txtMontant;
	@FXML
	private TextField txtNumCompte;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.operationResultat = null;
		this.primaryStage.close();
	}

	@FXML
	private void doAjouter() {
		switch (this.categorieOperation) {
		case DEBIT:
			// règles de validation d'un débit :
			// - le montant doit être un nombre valide
			// - et si l'utilisateur n'est pas chef d'agence,
			// - le débit ne doit pas amener le compte en dessous de son découvert autorisé
			double montant;

			this.txtMontant.getStyleClass().remove("borderred");
			this.lblMontant.getStyleClass().remove("borderred");
			this.lblMessage.getStyleClass().remove("borderred");
			String info = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info);

			try {
				montant = Double.parseDouble(this.txtMontant.getText().trim());
				if (montant <= 0)
					throw new NumberFormatException();
			} catch (NumberFormatException nfe) {
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
			if(indiceAdmin == false) { //si l'utilisateur n'est pas chef d'agence, on affiche les bonnes infos
			 if (this.compteEdite.solde - montant < this.compteEdite.debitAutorise) {
				info = "Dépassement du découvert ! - Cpt. : " + this.compteEdite.idNumCompte + "  "
						+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
						+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
				this.lblMessage.setText(info);
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblMessage.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			 }
			}
			
			String typeOp = this.cbTypeOpe.getValue();
			this.operationResultat = new Operation(-1, montant, null, null, this.compteEdite.idNumCli, typeOp);
			this.primaryStage.close();
			break;
		case CREDIT:
			//règle de validation d'un crédit :
			//le montant doit être un montant valide (montantCredit > 0)
			double montantCredit;

			this.txtMontant.getStyleClass().remove("borderred");
			this.lblMontant.getStyleClass().remove("borderred");
			this.lblMessage.getStyleClass().remove("borderred");
			String infoc = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(infoc);
			
			try {
				montantCredit = Double.parseDouble(this.txtMontant.getText().trim());
				if (montantCredit <= 0)
					throw new NumberFormatException();
				} catch (NumberFormatException nfe) {
					this.txtMontant.getStyleClass().add("borderred");
					this.lblMontant.getStyleClass().add("borderred");
					this.txtMontant.requestFocus();
					return;
				}
				
			
			String typeOpCredit = this.cbTypeOpe.getValue();
			this.operationResultat = new Operation(-1, montantCredit, null, null, this.compteEdite.idNumCli, typeOpCredit);
			this.primaryStage.close();
			break;
		case VIREMENT:
			//règle de validation d'un crédit :
			//le montant doit être un montant valide (montantCredit > 0)
			//le champ "montant" doit être renseigné
			//le champ "vers compte numéro :" doit être renseigné
			//le débit ne doit pas amener le compte en dessous de son découvert autorisé
			double montantV;

			this.txtMontant.getStyleClass().remove("borderred");
			this.lblMontant.getStyleClass().remove("borderred");
			this.lblMessage.getStyleClass().remove("borderred");
			this.lblNumCompte.getStyleClass().remove("borderred");
			String infoV = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(infoV);
			
			try {
				montantV = Double.parseDouble(this.txtMontant.getText().trim());
				if (montantV <= 0 || this.txtNumCompte.getText().equals(""))
					throw new NumberFormatException();
				} catch (NumberFormatException nfe) {
					this.txtMontant.getStyleClass().add("borderred");
					this.lblMontant.getStyleClass().add("borderred");
					this.lblNumCompte.getStyleClass().add("borderrred");
					this.txtMontant.requestFocus();
					return;
				}
			
			if (this.compteEdite.solde - montantV < this.compteEdite.debitAutorise) {
				info = "Dépassement du découvert ! - Cpt. : " + this.compteEdite.idNumCompte + "  "
						+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
						+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
				this.lblMessage.setText(info);
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblMessage.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
				
			
			String typeOpV = this.cbTypeOpe.getValue();
			this.operationResultat = new Operation(-1, montantV, null, null, this.compteEdite.idNumCli, typeOpV);
			this.primaryStage.close();
			break;
		}
	}
}