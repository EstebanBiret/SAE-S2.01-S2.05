package application.control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.ComptesManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.CompteCourant;
import model.orm.AccessCompteCourant;
import model.orm.LogToDatabase;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.Table;

public class ComptesManagement {

	/**
	 * Attributs
	 */
	
	private Stage primaryStage; //la fenêtre principale
	private ComptesManagementController cmc; //le controller relié à la gestion d'un compte
	private DailyBankState dbs;
	private Client clientDesComptes; //un client ayant un compte

	/**
	 * @param _parentStage
	 * @param _dbstate
	 * @param client
	 * gère la fenêtre "gestion de compte" après avoir cliqué sur le bouton "comptes client"
	 */
	public ComptesManagement(Stage _parentStage, DailyBankState _dbstate, Client client) {

		this.clientDesComptes = client;
		this.dbs = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(ComptesManagementController.class.getResource("comptesmanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+50, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des comptes");
			this.primaryStage.setResizable(false);

			this.cmc = loader.getController();
			this.cmc.initContext(this.primaryStage, this, _dbstate, client);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doComptesManagementDialog() {
		this.cmc.displayDialog();
	}

	/**
	 * permet de gérer les opérations d'un compte
	 * @param cpt le compte
	 */
	public void gererOperations(CompteCourant cpt) {
		OperationsManagement om = new OperationsManagement(this.primaryStage, this.dbs, this.clientDesComptes, cpt);
		om.doOperationsManagementDialog();
	}

	/**
	 * permet de créer un compte
	 * @return le compte crée
	 * @throws SQLException 
	 */
	public CompteCourant creerCompte() throws SQLException {
		CompteCourant compte;
		CompteEditorPane cep = new CompteEditorPane(this.primaryStage, this.dbs);
		compte = cep.doCompteEditorDialog(this.clientDesComptes, null, EditionMode.CREATION);
		if (compte != null) {
			try {
				
				Connection con = LogToDatabase.getConnexion(); //Connexion à la base de données
                
                String query = "INSERT INTO COMPTECOURANT VALUES (" + "seq_id_client.NEXTVAL" + ", " + "?" + ", " + "?" + ", " + "?" + ", " + "?" + ")";
                
                PreparedStatement pst = con.prepareStatement(query);
                pst.setInt(1, compte.debitAutorise);
                pst.setDouble(2, compte.solde);
                pst.setInt(3, compte.idNumCli);
                pst.setString(4, compte.estCloture);
                
                int result = pst.executeUpdate();
                pst.close();
				
				
                
                if (result != 1) {
                    con.rollback();
                    System.out.println("erreur lors du insert");
                }else {
                    con.commit();
                }
                
				// existe pour compiler les catchs dessous
				if (Math.random() < -1) {
					throw new ApplicationException(Table.CompteCourant, Order.INSERT, "todo : test exceptions", null);
				}
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
			}
		}
		return compte;
	}
	
	/**
	 * Permet de modifier le compte d'un client
	 * @param compte : compte du client selectionné
	 * @return compte modifié
	 */
	public CompteCourant modifierCompte(CompteCourant compte) {
		CompteEditorPane cep = new CompteEditorPane(this.primaryStage, this.dbs);
		CompteCourant result = cep.doCompteEditorDialog(this.clientDesComptes, compte, EditionMode.MODIFICATION);
		if(result != null) {
			try {
				AccessCompteCourant acpt = new AccessCompteCourant();
				acpt.updateCompteCourant(result);
				
			}catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				result = null;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				result = null;
			}
		}
		
		return result;
	}

	
	/**
	 * @return une liste de compteCourant en fonction du client sélectionné
	 */
	public ArrayList<CompteCourant> getComptesDunClient() {
		ArrayList<CompteCourant> listeCpt = new ArrayList<>();

		try {
			AccessCompteCourant acc = new AccessCompteCourant();
			listeCpt = acc.getCompteCourants(this.clientDesComptes.idNumCli);
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeCpt = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
			listeCpt = new ArrayList<>();
		}
		return listeCpt;
	}
}