package model.data;


public class Emprunt {
    public double capitalEmprunt;
    public int dureeEmprunt;
    public double tauxPretAnnuel;
    public double tauxAssurance;
    
    
    public Emprunt(double capital, int duree, double tauxPret) {
        this.capitalEmprunt = capital;
        this.dureeEmprunt = duree;
        this.tauxPretAnnuel = tauxPret;
    }
    
    public Emprunt(double capital, int duree, double tauxPret, double tauxAssurance) {
        this.capitalEmprunt = capital;
        this.dureeEmprunt = duree;
        this.tauxPretAnnuel = tauxPret;
        this.tauxAssurance = tauxAssurance;
    }
public double getCapitalEmprunt() {
        return capitalEmprunt;
    }
    
    public int getPeriodeEmprunt() {
        return dureeEmprunt;
    }
    
    public double getTauxPretAnnuel() {
        return tauxPretAnnuel;
    }
public double getTauxApplicable() {
        double res = this.tauxPretAnnuel/100/12;
        return res;
    }
    
    public int getNbPeriode() {
        int res = this.dureeEmprunt*12;
        return res;
    }
    
    public double getMensualiteAssurance() {
        return this.tauxAssurance/100*(this.capitalEmprunt/12);
    }
    
    public double getMensualiteSansAss() {
        return this.capitalEmprunt*(this.getTauxApplicable()/ (1-Math.pow(1+this.getTauxApplicable(), -this.getNbPeriode())) );
    }
    
    public double getMensualiteAvecAss() {
        return this.getMensualiteSansAss()+this.getMensualiteAssurance();
    }
    

    
    public double coutCredit() {
        double capitalRestant = this.capitalEmprunt;
        double res = 0;
        
        do {
            double interet = capitalRestant*this.getTauxApplicable();
            double montantDuPricipal = this.getMensualiteSansAss() - interet;
            capitalRestant = capitalRestant - montantDuPricipal;
            res+=interet;
        }while(capitalRestant>0);
        
        
        return res;
    }
    
    public double coutAssurance() {
        return this.getMensualiteAssurance()*this.getNbPeriode();
    }
    
    public double coutTotal(double frais) {
        return (coutAssurance()+coutCredit())+frais;
    }
}