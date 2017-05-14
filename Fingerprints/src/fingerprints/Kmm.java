package fingerprints;
import java.awt.image.*;
import java.util.*;


public final class Kmm
{
    private static final boolean[] buildKillsArray(int[] kills)
    {
        boolean[] ar = new boolean[256];
        Arrays.fill(ar, false);
        for(int i = 0; i < kills.length; ++i)
            ar[kills[i]] = true;
        return ar;
    }
    private static final boolean[] killsRound = buildKillsArray(new int[]{
            3, 12,  48, 192, 6, 24,  96, 129,	//	-	2 s�siad�w
            14, 56, 131, 224, 7, 28, 112, 193,	//	-	3 s�siad�w
            195, 135, 15, 30, 60, 120, 240, 225,//	-	4 s�siad�w
//			31, 62, 124, 248, 241, 227, 199, 143,//	-	5 s�siad�w
//			63, 126, 252, 249, 243, 231, 207, 159,//-	6 s�siad�w
//			254, 253, 251, 247, 239, 223, 190, 127,//-	7 s�siad�w
    });
    private static final boolean canRound(int weight)
    {
        return killsRound[weight];
    }
    private static final boolean[] killsFinish = buildKillsArray(new int[]{
            5,   13,  15,  20,  21,  22,  23,  29,
            30,  31,  52,  53,  54,  55,  60,  61,
            62,  63,  65,  67,  69,  71,  77,  79,
            80,  81,  83,  84,  85,  86,  87,  88,
            89,  91,  92,  93,  94,  95,  97,  99,
            101, 103, 109, 111, 113, 115, 116, 117,
            118, 119, 120, 121, 123, 124, 125, 126,
            127, 133, 135, 141, 143, 149, 151, 157,
            159, 181, 183, 189, 191, 195, 197, 199,
            205, 207, 208, 209, 211, 212, 213, 214,
            215, 216, 217, 219, 220, 221, 222, 223,
            225, 227, 229, 231, 237, 239, 240, 241,
            243, 244, 245, 246, 247, 248, 249, 251,
            252, 253, 254, 255,

            3,  12,  48, 192,	//	-	2 s�siad�w
            14, 56, 131, 224,	//	-	3 s�siad�w 'I'
            7,  28, 112, 193,	//	-	3 s�siad�w 'L'
    });
    private static final boolean canKill(int weight)
    {
        return killsFinish[weight];
    }

    /*
            nw=    image[i-1][j-1]
            n=     image[i][j-1]
            ne=    image[i+1][j-1]
            w=     image[i-1][j]
            point= image[i][j]
            e=     image[i+1][j]
            sw=    image[i-1][j+1]
            s=     image[i][j+1]
            se=    image[i+1][j+1]

     */
    public int[][] thin(final int[][] imageOrgi,int height,int width)
    {
        int[][]image=imageOrgi;
        boolean thinned;
        do
        {
            thinned = true;
            // BORDER
            for(int i=1;i<width-1;i++){
                for (int j=1;j<height-1;j++){
                    if(image[i][j]!=0){
                        //n s e w
                        if(image[i][j-1]==0 || image[i][j+1]==0 || image[i+1][j]==0 || image[i-1][j]==0){
                                image[i][j]=3;
                        }
                        //ne nw se sw
                        else if(image[i+1][j-1]==0 || image[i-1][j-1]==0 || image[i+1][j+1]==0 || image[i-1][j+1]==0  ){
                            image[i][j]=4;
                        }
                    }
                }
            }
            // ROUND
            for(int i=1;i<width-1;i++){
                for (int j=1;j<height-1;j++){
                    if(image[i][j]!=0){
                       if(image[i][j]!=3)continue;
                       if(canRound(calculateWeight(i,j,image))){
                           image[i][j]=2;
                       }
                    }
                }
            }
            // CLEAR
            for(int i=1;i<width-1;i++){
                for (int j=1;j<height-1;j++){
                    if(image[i][j]!=0){
                       if(image[i][j]==2){
                           image[i][j]=0;
                           thinned=false;
                       }
                    }
                }
            }
            // FINISH A
            for(int i=1;i<width-1;i++){
                for (int j=1;j<height-1;j++){
                    if(image[i][j]!=0){
                        if(image[i][j]!=3)continue;
                        if(canKill(calculateWeight(i,j,image))){
                            image[i][j]=0;
                            thinned=false;
                        }
                        else{
                            image[i][j]=1;
                        }
                    }
                }
            }

            // FINISH B
            for(int i=1;i<width-1;i++){
                for (int j=1;j<height-1;j++){
                    if(image[i][j]!=0){
                       if(image[i][j]!=4)continue;
                       if(canKill(calculateWeight(i,j,image))){
                           image[i][j]=0;
                           thinned=false;
                       }
                       else{
                           image[i][j]=1;
                       }
                    }
                }
            }


        }while(!thinned);


       return image;
    }	

    private int calculateWeight(int i, int j,int[][]image) {
        int count=0;

        if(image[i-1][j-1]!=0)count+=128;
        if(image[i][j-1]!=0)count+=1;
        if(image[i+1][j-1]!=0)count+=2;
        if(image[i-1][j]!=0)count+=64;
        if(image[i+1][j]!=0)count+=4;
        if(image[i-1][j+1]!=0)count+=32;
        if(image[i][j+1]!=0)count+=16;
        if(image[i+1][j+1]!=0)count+=8;

        return count;
    }

}