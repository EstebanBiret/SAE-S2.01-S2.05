package application.view;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import application.DailyBankState;
import application.control.ClientsManagement;
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
import model.data.Client;
import model.data.CompteCourant;
import model.data.Operation;
import model.orm.AccessClient;
import model.orm.AccessCompteCourant;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.ManagementRuleViolation;
import model.orm.exception.RowNotFoundOrTooManyRowsException;

public class ClientsManagementController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private ClientsManagement cm;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private ObservableList<Client> olc;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, ClientsManagement _cm, DailyBankState _dbstate) {
		this.cm = _cm;
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.olc = FXCollections.observableArrayList();
		this.lvClients.setItems(this.olc);
		this.lvClients.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvClients.getFocusModel().focus(-1);
		this.lvClients.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
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
	private TextField txtNom;
	@FXML
	private TextField txtPrenom;
	@FXML
	private ListView<Client> lvClients;
	@FXML
	private Button btnDesactClient;
	@FXML
	private Button btnModifClient;
	@FXML
	private Button btnComptesClient;

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
	 * Permet de rechercher un client avec des informations spécifiques
	 * si les informations ne sont pas présentes, elle seront vides dans le ListView
	 */
	@FXML
	private void doRechercher() {
		int numCompte;
		try {
			String nc = this.txtNum.getText();
			if (nc.equals("")) {
				numCompte = -1;
			} else {
				numCompte = Integer.parseInt(nc);
				if (numCompte < 0) {
					this.txtNum.setText("");
					numCompte = -1;
				}
			}
		} catch (NumberFormatException nfe) {
			this.txtNum.setText("");
			numCompte = -1;
		}

		String debutNom = this.txtNom.getText();
		String debutPrenom = this.txtPrenom.getText();

		if (numCompte != -1) {
			this.txtNom.setText("");
			this.txtPrenom.setText("");
		} else {
			if (debutNom.equals("") && !debutPrenom.equals("")) {
				this.txtPrenom.setText("");
			}
		}

		// Recherche des clients en BD. cf. AccessClient > getClients(.)
		// numCompte != -1 => recherche sur numCompte
		// numCompte != -1 et debutNom non vide => recherche nom/prenom
		// numCompte != -1 et debutNom vide => recherche tous les clients
		ArrayList<Client> listeCli;
		listeCli = this.cm.getlisteComptes(numCompte, debutNom, debutPrenom);

		this.olc.clear();
		for (Client cli : listeCli) {
			this.olc.add(cli);
		}

		this.validateComponentState();
	}
	
	/*
	 * Affiche le compte d'un client
	 */
	@FXML
	private void doComptesClient() {
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		// ouvre une fenêtre pour gérer les opérations du client
		if (selectedIndice >= 0) {
			Client client = this.olc.get(selectedIndice);
			this.cm.gererComptesClient(client);
		}
	}
	
	/*
	 * Permet de modifier les informations d'un client
	 */
	@FXML
	private void doModifierClient() {

		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Client cliMod = this.olc.get(selectedIndice);
			Client result = this.cm.modifierClient(cliMod);
			if (result != null) {
				this.olc.set(selectedIndice, result);
			}
		}
	}
	
	/*
	 * Permet de désactiver (supprimer) un client
	 */
	@FXML
	private void doDesactiverClient() throws ManagementRuleViolation, RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Client cliDesac = this.olc.get(selectedIndice);
			AccessClient ac = new AccessClient();
			AccessCompteCourant acc = new AccessCompteCourant();
			
			switch(cliDesac.getEstInactif()) {
				case "O":
					System.out.println("est Inactif");
					cliDesac.setEstInactif("N");
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Réactiver le client ?");
					alert.setHeaderText("Voulez-vous réactiver le client ?");
					alert.showAndWait().ifPresent(response -> {
						if(response == ButtonType.OK) {
							try {
								ac.updateClient(cliDesac);
								acc.openCompteClient(cliDesac);
							} catch (RowNotFoundOrTooManyRowsException | DataAccessException | DatabaseConnexionException | ManagementRuleViolation e) {
								ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
								ed.doExceptionDialog();
							}
						}
					});
					break;
				case "N":
					System.out.println("Inactif");
					cliDesac.setEstInactif("O");
					try {
						ac.updateClient(cliDesac);
						acc.closeCompteClient(cliDesac);
					} catch (RowNotFoundOrTooManyRowsException | DataAccessException | DatabaseConnexionException e) {
						ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
						ed.doExceptionDialog();
					}
					break;
			}
			
			System.out.println("Est devenu : " + cliDesac.getEstInactif());
		}
		
		this.doRechercher();
		/*
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Client cliDesac = this.olc.get(selectedIndice);
			cliDesac.setEstInactif("O");
			AccessClient ac = new AccessClient();
			try {
				ac.updateClient(cliDesac);
			} catch (RowNotFoundOrTooManyRowsException | DataAccessException | DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
			}
			System.out.println(cliDesac.getEstInactif());
		}
	
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Désactiver le client");
		alert.setHeaderText("Voulez vous réellement désactiver le client ?");
		alert.showAndWait();
		*/
		
	}
	
	/*
	 * Permet d'ajouter un nouveau client
	 */
	@FXML
	private void doNouveauClient() {
		Client client;
		client = this.cm.nouveauClient();
		if (client != null) {
			this.olc.add(client);
		}
	}
	
	
	
	
	/*
	 * Permet de désactiver certains boutons
	 */
	private void validateComponentState() {
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		Client client = this.lvClients.getSelectionModel().getSelectedItem();
		if (selectedIndice >= 0 && client.estInactif.equals("O")) {
			this.btnModifClient.setDisable(false);
			this.btnComptesClient.setDisable(false);
			this.btnDesactClient.setDisable(false);
			this.btnDesactClient.setText("Réactiver Client");
		} else {
			this.btnModifClient.setDisable(false);
			this.btnComptesClient.setDisable(false);
			this.btnDesactClient.setDisable(false);
			this.btnDesactClient.setText("Désactiver Client");
		}
		
		
	}
	

}