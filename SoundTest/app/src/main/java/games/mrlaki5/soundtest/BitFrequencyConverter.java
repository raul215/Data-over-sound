package games.mrlaki5.soundtest;

import java.util.ArrayList;

public class BitFrequencyConverter {

    private int numberOfBitsInOneTone;
    private int startFrequency;
    private int endFrequency;
    private int padding;
    private int handshakeStartFreq;
    private int handshakeEndFreq;
    private int handshakePadding;

    private ArrayList<Byte> readBytes;
    private byte currByte;
    private int currShift;

    public BitFrequencyConverter(int startFrequency, int endFrequency, int numberOfBitsInOneTone){
        this.numberOfBitsInOneTone=numberOfBitsInOneTone;
        this.padding=((endFrequency-750)-startFrequency)/(2+(int)Math.pow(2, numberOfBitsInOneTone));

        this.handshakeEndFreq=endFrequency;
        this.handshakeStartFreq=endFrequency-500;
        this.handshakePadding=250;

        this.startFrequency=startFrequency;
        this.endFrequency=endFrequency-750;

        readBytes=new ArrayList<Byte>();
        currByte=0x00;
        currShift=0;
    }

    public void calculateBits(double frequency){
        byte resultBytes=0x00;
        boolean freqFound=false;
        boolean lastPart=false;
        int counter=0;
        for(int i=(startFrequency); i<=(endFrequency); i+=padding, counter++){
            if(frequency>=(i-(padding/2)) && (frequency<=(i+(padding/2)))){
                if(counter==0 || counter==1){
                    lastPart=true;
                }
                else{
                    freqFound=true;
                }
                break;
            }
            else{
                if(counter!=0 && counter!=1) {
                    resultBytes += 0x01;
                }
            }
        }
        if(freqFound){
            int tempCounter=numberOfBitsInOneTone;
            while(tempCounter>0){
                byte mask=0x01;
                mask<<=(tempCounter-1);
                currByte<<=1;
                if((mask&resultBytes)!=0x00){
                   currByte+=0x01;
                }
                currShift++;
                if(currShift==8){
                    readBytes.add(currByte);
                    currShift=0;
                    currByte=0x00;
                }
                tempCounter--;
            }
        }
        else {
            if (lastPart){
                currByte<<=1;
                if(counter==1){
                    currByte+=0x01;
                }
                currShift++;
                if(currShift==8){
                    readBytes.add(currByte);
                    currByte=0x00;
                    currShift=0;
                }
            }
        }
    }

    protected int specificFrequency(byte sample){
        int freq=startFrequency+padding*2;
        int numberOfFreq=(int)Math.pow(2, numberOfBitsInOneTone);
        byte tempByte=0x00;
        for(int i=0; i<numberOfFreq; i++){
            if(tempByte==sample){
                break;
            }
            tempByte+=0x01;
            freq+=padding;
        }
        return freq;
    }

    public int getBit(byte check ,int position){
        return (check >> position) & 1;
    }

    public ArrayList<Integer> calculateFrequency(byte[] byteArray){
        ArrayList<Integer> resultList=new ArrayList<Integer>();
        boolean isDataModulo=(byteArray.length*8 % numberOfBitsInOneTone)==0;
        byte currByte=0x00;
        int currShift=0;
        for(int i=0; i<byteArray.length; i++){
            byte tempByte=byteArray[i];
            for(int j=7; j>=0; j--){
                if(((currShift+j+1+(byteArray.length-(i+1))*8)<numberOfBitsInOneTone) && (!isDataModulo)){
                    int temp=getBit(tempByte, j);
                    if(temp==1){
                        resultList.add(startFrequency+padding);
                    }
                    else{
                        resultList.add(startFrequency);
                    }
                    continue;
                }
                int temp=getBit(tempByte, j);
                currByte<<=1;
                if(temp==1){
                    currByte+=0x01;
                }
                currShift++;
                if(currShift==numberOfBitsInOneTone){
                    int currFreq=specificFrequency(currByte);
                    resultList.add(currFreq);
                    currByte=0x00;
                    currShift=0;
                }
            }
        }
        return resultList;
    }

    public byte[] getReadBytes(){
        byte[] retArr;
        if(currShift!=0){
            retArr= new byte[readBytes.size()+1];
            retArr[retArr.length-1]=currByte;
        }
        else{
            retArr= new byte[readBytes.size()];
        }
        int i=0;
        for (byte tempB : readBytes) {
            retArr[i]=tempB;
            i++;
        }
        return retArr;
    }

    public int getPadding() {
        return padding;
    }

    public int getHandshakePadding(){
        return handshakePadding;
    }

    public int getHandshakeStartFreq() {
        return handshakeStartFreq;
    }

    public int getHandshakeEndFreq() {
        return handshakeEndFreq;
    }
}
