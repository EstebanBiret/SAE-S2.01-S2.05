package application.view;

import java.net.URL;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.SimulationEmpruntPane;
import application.tools.AlertUtilities;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Emprunt;

public class SimulationEmpruntController implements Initializable {


	private Emprunt emprunt;


	private Stage primaryStage;




	public void initContext(Stage _primaryStage) {
		this.primaryStage = _primaryStage;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.validateComponent();
	}


	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}


	@FXML
	private TextField capital;
	@FXML
	private TextField duree;
	@FXML
	private TextField tauxAnnuel;
	@FXML
	private TextField tauxApplicable;
	@FXML
	private TextField nbPeriodes;
	@FXML
	private TextField mensualiteSansA;
	@FXML
	private TextField coutCredit;
	@FXML
	private TextField fraisDossier;
	@FXML
	private TextField total;
	@FXML
	private TextField tauxAssurance;
	@FXML
	private TextField mensualiteAssurance;
	@FXML
	private TextField mensualiteAvecA;
	@FXML
	private TextField coutAssurance;
	@FXML
	private Button simuler;

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

	private void setTauxApplicable(TextField txt, Emprunt em) {
		String res = String.valueOf(em.getTauxApplicable());
		txt.setText(res);
	}

	private void setPeriode(TextField txt, Emprunt em) {
		String res = String.valueOf(em.getNbPeriode());
		txt.setText(res);
	}

	private void setMensualiteSansAssurance(TextField txt, Emprunt em) {
		String res = String.valueOf(em.getMensualiteSansAss());
		txt.setText(res);
	}

	private void setMensualiteAvecAssurance(TextField txt, Emprunt em) {
		String res = String.valueOf(em.getMensualiteAvecAss());
		txt.setText(res);
	}

	private void setMensualiteAssurance(TextField txt, Emprunt em) {
		String res = String.valueOf(em.getMensualiteAssurance());
		txt.setText(res);
	}

	private void setCoutCredit(TextField txt, Emprunt em) {
		String res = String.valueOf(em.coutCredit());
		txt.setText(res);
	}

	private void setCoutAssurance(TextField txt, Emprunt em) {
		String res = String.valueOf(em.coutAssurance());
		txt.setText(res);
	}

	private void setTotal(TextField txt, Emprunt em) {
		String res = String.valueOf(em.coutTotal(toDouble(this.fraisDossier.getText().trim())));
		txt.setText(res);
	}

	@FXML
	private void displayEmprunt() {
		if (this.isSaisieValide()) {
			setTauxApplicable(tauxApplicable, emprunt);
			setPeriode(nbPeriodes, emprunt);
			setMensualiteSansAssurance(mensualiteSansA, emprunt);
			setMensualiteAvecAssurance(mensualiteAvecA, emprunt);
			setMensualiteAssurance(mensualiteAssurance, emprunt);
			setCoutAssurance(coutAssurance, emprunt);
			setCoutCredit(coutCredit, emprunt);
			setTotal(total, emprunt);
			 Alert alert = new Alert(AlertType.INFORMATION);
			 alert.setTitle("Information");
			 alert.setHeaderText("Création de l'emprunt");
			 alert.setContentText(" L'emprunt est valide ");

		 	 alert.showAndWait();
		}else {
			 Alert alert = new Alert(AlertType.ERROR);
			 alert.setTitle("ERREUR");
			 alert.setHeaderText("Création de l'emprunt");
			 alert.setContentText("Création de l'emprunt IMPOSSIBLE, une données est invalide");

		 	 alert.showAndWait();
			
		}
		
	}

	
	private double toDouble(String number) {
		try {
			return Double.parseDouble(number);
		} catch (Exception e) {
			return -1;
		}
	}

	/** Permet de vérifier la validiter des champs
     * @return True si les données sont valides, False sinon
     */
    private boolean isSaisieValide() {
    	//créé un emprunt depuis les textfield
    	emprunt = new Emprunt( toDouble(this.capital.getText().trim()), (int) toDouble(this.duree.getText().trim()), toDouble(this.tauxAnnuel.getText().trim()), toDouble(this.tauxAssurance.getText().trim()));
      
      
        
       
	
			if (emprunt.capitalEmprunt < 10000) {
				return false; 
			}
			if (emprunt.dureeEmprunt < 2) {
				return false; 
			}
	
			if (emprunt.tauxPretAnnuel < 0) {
				return false; 
			}
		
			if (emprunt.tauxAssurance < 0) {
				return false; 
			}
			if (toDouble(this.fraisDossier.getText().trim()) < 0) {
				return false;  
			}
			
				return true; 
			
			}

	private void validateComponent() {
		this.tauxApplicable.setEditable(false);
		this.nbPeriodes.setEditable(false);
		this.coutCredit.setEditable(false);
		this.mensualiteAssurance.setEditable(false);
		this.mensualiteSansA.setEditable(false);
		this.mensualiteAvecA.setEditable(false);
		this.coutAssurance.setEditable(false);
		this.total.setEditable(false);
	}

}