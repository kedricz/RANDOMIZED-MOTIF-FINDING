/*  
 *  Random Motif Finding 
 *  Name: Cedric Jo
 *  Date: 05/07/2014
 */

package rmf;

import java.io.*;
import java.util.*;


public class RMF {
    String[] dna;       // dna sequences
    int t;              // number of sequences
    int n;              // sequence length
    int l;              // motif length
    int[] s;            // starting positions
    int score;          // consensus score
    int iteration;      // number of iterations
    char[] bestMotif;   // Motif
    int rndSeq;         // Choosen sequence (0-3)
    int[][] p;          // profile
    
    
    RMF (String[] dna, int t, int n, int l) {
        this.dna = dna;
        this.t = t;
        this.n = n;
        this.l = l; 
        this.s = new int[t];
        this.score = 0;
        this.iteration= 0;
        this.bestMotif = new char[this.l];
        this.p = new int[t][l];
        
        gibbs();       
    }
    
    
    public void gibbs() {
       // 1. Generate a set of starting positions 
       startP();
              
       
       while(score < 16) {
           
       // 2. Randomly choose one of the t sequences
       rndSeq =  (1+(int)(Math.random()*((this.t-1)+1)))-1;

       
       // 3. Create profile P from the other t-1 sequences
       profile();
       
       
       // 4. Calculate the probability that the l-mer starting at that 
       //    position was generated by P
       double[][] pb = prob();
       
       
       // 5. Choose new starting position for the removed sequence
       int ns = newStart(pb);   // ns = new starting position
       s[rndSeq] = ns;      // Update starting position

       
       // Calculate consensus score with current starting positions
       score();     
       iteration++;
       
       }
       
       
       System.out.print("Starting positions = ");
       for(int i=0; i < t; i++) {
           System.out.print(s[i] + "  ");
       }
       System.out.println();
       
       System.out.println("consensus score = " + score);
       
       System.out.println("Number of iteration = " + iteration);
       
       
//       for(int a=0; a < t; a++) {        
//       int begin = s[a];
//          for(int b=0; b < l; b++) {
//            System.out.print(dna[a].charAt(begin+b));
//            }
//          System.out.println();
//        }
       
       bestMotif(s);
       String motif = new String(this.bestMotif);
       System.out.println("MOTIF => " + motif);
    }
    
    
    // Randomly choose starting positions
    public void startP() {
        for(int i=0; i < t; i++) {
            int rnd =  1+(int)(Math.random()*((this.n-l)+1));
            s[i] = rnd-1;
        }
    }
    
    
    // Create profile P
    public void profile() {
        p = new int[t][l];
        char[][] pSeq = new char[t-1][l];
        
        if(rndSeq==t-1) {
            for(int a=0; a < t-1; a++) {
                
                int begin = s[a];
                for(int b=0; b < l; b++) {
                    pSeq[a][b]=dna[a].charAt(begin+b);
                }
            }
        }
            
        else {
            int tmp=0;
            outerloop:
            for(int a=0; a < t; a++) {
                
                int begin = s[a];
                for(int b=0; b < l; b++) {      

                    if(a==rndSeq) {
                        tmp=a+1;
                        
                        for(int c=tmp; c < t; c++) {
                            begin = s[c];
                            for(int d=0; d < l; d++) {
                                pSeq[c-1][d]=dna[c].charAt(begin+d);
                            }
                        }
                        break outerloop;   
                    } // end if
                    
                    pSeq[a][b]=dna[a].charAt(begin+b);
                } // end innerloop
            } // end outerloop  
        } // end else
        
 
        for(int i=0; i < l; i++) {
            for(int j=0; j < t-1; j++) {
                if(pSeq[j][i]=='A') {
                    p[0][i]++;
                }
                else if(pSeq[j][i]=='C') {
                    p[1][i]++;
                }
                else if(pSeq[j][i]=='T') {
                    p[2][i]++;
                }
                else if(pSeq[j][i]=='G') {
                    p[3][i]++;
                }
            }
        }
        
  
//       System.out.println("---------- pSeq ----------------------------------");
//       for(int i=0; i < t-1; i++) {
//           for(int j=0; j < l; j++) {
//               System.out.print(pSeq[i][j] + "\t");
//           }
//           System.out.println();
//       }        
       
      
//       System.out.println("---------- p ----------------------------------");
//       for(int i=0; i < t; i++) {
//           for(int j=0; j < l; j++) {
//               System.out.print(p[i][j] + "\t");
//           }
//           System.out.println();
//       }         
    }
    
    
    // Calculate probabilities
    public double[][] prob() {
        double[][] pb = new double[n-l+1][2];
        
        for(int j=0; j < n-l+1; j++) {
            pb[j][0] = j;
        }
        
        
        int i=0;
        while(i < n-l+1) {
            
            for(int k=0; k < l; k++) {
               if(dna[rndSeq].charAt(i+k)=='A') {
                   pb[i][1] *= (double)(p[0][k]/t);
               }
               
               else if(dna[rndSeq].charAt(i+k)=='C') {
                   pb[i][1] *= (double)(p[1][k]/t);
               }
               
               else if(dna[rndSeq].charAt(i+k)=='T') {
                   pb[i][1] *= (double)(p[2][k]/t);
               }
               
               else if(dna[rndSeq].charAt(i+k)=='G') {
                   pb[i][1] *= (double)(p[3][k]/t);
               }
            } // end for
            i++;
        }
        
//        System.out.println("------------- pb ------------------");
//        for(int a=0; a < n-l+1; a++) {
//            for(int b=0; b < 2; b++) {
//                System.out.print(pb[a][b] + "\t");
//            }
//            System.out.println();
//        }
        return pb;
    }
    
    
    // Choose new starting position for the removed sequenced
    public int newStart(double[][] pb) {
        int newS=0;
        boolean zero = false;
/*----------------------------------------------------------------------------*/
        // When all the probabilities are zero
        int i=0;
        while(i < n-l+1) {
            if(pb[i][1] != 0) {
                zero = true;
                break;
            }
            i++;
        } // end while
        
        if(!zero) {
            Random r = new Random();
            double x = r.nextDouble();
            
            //System.out.println("x = " + x);
            
            double a = (double)1/(n-l+1);
                        
            int j=1;
            while(j < n-l+2){
                if(x < a*j) {
                    newS = j-1;
                    break;
                }
                j++;
            }
        } // end if
/*----------------------------------------------------------------------------*/        
        else {
        ArrayList<Integer> startP = new ArrayList<>();
        int k=0;
        while(i < n-l+1) {
            if(pb[k][1]!=0) {
               startP.add((int)pb[k][0]);
            }
            k++;
        } // end while
        
        System.out.println();
        for(int f=0; f < startP.size(); f++) {
            System.out.print(startP.get(f) + "\t");
        }
        
        // Find lowest probability
        double min=pb[startP.get(0)][1];
        for(int v=1; v < startP.size(); v++) {
            if(pb[startP.get(v)][1] < min) {
                min = pb[startP.get(v)][1];
            }
        }
        
        // tmp contains each probability divided by the lowest probability
        ArrayList<Double> tmp = new ArrayList<>();
        for(int v=0; v < startP.size(); v++) {
            double prtmp = (pb[startP.get(v)][1])/min;
            tmp.add(prtmp);
        }
        
        double sumRatio=0;
        for(int n=0; n < startP.size(); n++) {
             sumRatio += tmp.get(n);
        }
        
        
        // tmp2 = Normalized Probabilities between 0-1
        ArrayList<Double> tmp2 = new ArrayList<>();
        for(int m=0; m < startP.size(); m++) {
            double pr = tmp.get(m)/sumRatio;
            tmp2.add(pr);
        }
        
        // Highest probability
        double hb = Collections.max(tmp2);
        int index=0;
        for(int z=0; z < tmp2.size(); z++) {
            if(tmp2.get(z).equals(hb)) {
                index = z;
            }
        }  
        newS = startP.get(index);
        }
        return newS;
    }
    
    
    
    // Calculate consensus score
    public void score() {
        score = 0;
        int max = 0;
        int sum = 0;
        int [][]count = new int[4][this.l];     // A C T G
        
        
        for(int i=0; i < t; i++) {
            for(int j=0; j < l; j++) {
                if(dna[i].charAt(j+s[i])=='A'){
                    count[0][j]++;
                }
                
                else if(dna[i].charAt(j+s[i])=='C'){
                    count[1][j]++;
                }
                
                else if(dna[i].charAt(j+s[i])=='T'){
                    count[2][j]++;
                }
                
                else if(dna[i].charAt(j+s[i])=='G'){
                    count[3][j]++;
                }   
            }
        }
        
        
//        for (int x=0; x < 4; x++) 
//        {
//            for (int y=0; y < this.l; y++)
//            { 
//                System.out.print(count[x][y] + "\t");
//            }
//            System.out.println();
//        }
        
        

        for(int h=0; h < l; h++) {
            max=0;
            for(int k=0; k < t; k++) {
                if(count[k][h] > max) {
                    max = count[k][h];
                }
            }
//            System.out.print(max + "\t");
            sum += max;
        }
        score = sum;
    }
    
    
   
    
    // Find best Motif
    public void bestMotif(int a[])
    {
        char [][]best = new char[this.t][this.l];
        
        for (int u=0; u < this.t; u++)
        {
            
            for (int j=a[u]; j < (a[u]+this.l); j++)
            {
                int q=(j-a[u]);
                best[u][q] = this.dna[u].charAt(j);
            }
        }
        
        int sumA, sumC, sumG, sumT;
        for (int r=0; r < this.l; r++)
        {
            sumA = 0;
            sumC = 0;
            sumG = 0;
            sumT = 0;

            for (int h=0; h < this.t; h++)
            {
                if (best[h][r] == 'A')
                {
                    sumA++;
                }
                else if (best[h][r] == 'C')
                {
                    sumC++;
                }
                else if (best[h][r] == 'G')
                {
                    sumG++;
                }
                else if (best[h][r] == 'T')
                {
                    sumT++;        
                }
                
                if (sumA > sumC && sumA > sumG && sumA > sumT)
                {
                    this.bestMotif[r] = 'A'; 
                }
                else if (sumC > sumA && sumC > sumG && sumC > sumT)
                {
                    this.bestMotif[r] = 'C';
                }    
                else if (sumG > sumA && sumG > sumC && sumG > sumT)
                {
                    this.bestMotif[r] = 'G';
                }
                else if (sumT > sumA && sumT > sumC && sumT > sumG)
                {
                    this.bestMotif[r] = 'T';
                }
            }
        }
    }
    
    
    
    
    
    
    public static void main(String[] args) {
        
        
        try 
        {
            BufferedReader br2 = new BufferedReader (new FileReader("findmotif.txt"));
            String s2;
            int line = 0;
            while ((s2=br2.readLine()) != null)   
            {
                line++;
            }
            int t = line-1; 
            br2.close();
            //System.out.println("t=" + t);
            
            
            BufferedReader br3 = new BufferedReader (new FileReader("findmotif.txt"));
            String str = br3.readLine();
            int line2 = 0;
            while ((str = br3.readLine()) != null)   
            {
                if (line2 == 2) break;
                line2++;
            }

            int counter = 0;
            for (int i=0; i < str.length(); i++)
            {
                if (Character.isLetter(str.charAt(i)))
                counter++;
            }
            int n = counter;
            br3.close();
            //System.out.println("n=" + n);
            
            
            BufferedReader br = new BufferedReader (new FileReader("findmotif.txt"));
            String s = br.readLine();
            int l = Integer.parseInt(s);
            //System.out.println("l=" + l);
            
            String[] dna = new String[t];
            for (int i=0; i < t; i++)
            {
                dna[i] = br.readLine();
                //System.out.println(dna[i]);
            }
            
            RMF m = new RMF(dna, t, n, l);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
}
