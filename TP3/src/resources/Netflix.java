package resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Netflix {
    private int id;
    private char[] type = new char[7];
    private String title;
    private String director;
    private long date;

    public Netflix() {

    }

    public Netflix(int id, char[] type, String title, String director, long date) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.director = director;
        this.date = date;

    }
    //construtor para método create, que atribui id novo de acordo com antigo último
    public Netflix(char[] type, String title, String director, long date) {
        this.type = type;
        this.title = title;
        this.director = director;
        this.date = date;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public char[] getType() {
        return type;
    }

    public void setType(char[] type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String toString() {
        Date data = new Date(date);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dataFormatada = sdf.format(data);
        return "\nID: " + id +
                "\nNome: " + title +
                "\nTipo: " + new String(type) +
                "\nDiretor: " + director +
                "\nData: " + dataFormatada + "\n";
    }

    //le linha do arquivo .csv e seta atributos da classe
    public void ler(String linha) {
        int j = 0;
		char c = ';';
		int tmp = 0;
		String vetorStr[] = new String[5];
        //separa atributos de cada linha do .csv usando ; como separador
		for(int i=0; i<linha.length(); i++){
			if(linha.charAt(i) == c) {
				vetorStr[j] =linha.substring(tmp, i);
				tmp = i+1;
				if(vetorStr[j].intern() == ""){
					vetorStr[j] = "nao informado";
				}
				j++;
			}
		}
		vetorStr[4] =linha.substring(tmp, linha.length());
        //seta data para 0 milisegundos se campo estiver vazio
        if(vetorStr[4].intern() == ""){
            setDate(0);
        //se data não estiver vazio usa função formatar data para transformar formato do .csv no formato dd/MM/yyyy
		} else {
            String data = formatarData(vetorStr[4]);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date dataFormatada = new Date();
            long unixTime = 0;
            try{
                dataFormatada = sdf.parse(data);
                unixTime = dataFormatada.getTime();
            }catch(Exception e){
                e.printStackTrace();
                e.getMessage();
            }
            setDate(unixTime);
        }
        //seta atributo id
        setId(Integer.parseInt(vetorStr[0]));

        //atribui valores do campo tipo a um vetor de tamanho fixo
        char[] tipo = new char[7];
        for(int i = 0; i < vetorStr[1].length(); i++){
            tipo[i] = vetorStr[1].charAt(i);
        }
        setType(tipo);//seta atributo tipo de acordo com vetor de char acima
        setTitle(vetorStr[2]);//seta atributo titulo
        setDirector(vetorStr[3]);//seta atributo diretor 

    }

    //função interna do programa específica para transformar a data do arquvo .csv em uma data padrão dd/Mm/yyyy
    //ex: October 28, 2004 em 28/10/2004
    private static String formatarData(String dataOriginal) {
        String[] partes = dataOriginal.split(" ");
        if(partes[0].equals("")){
            partes[0] = partes[1];
            partes[1] = partes[2];
            partes[2] = partes[3];
            partes[3] = null;
        }
        String mes = partes[0];
        String dia = partes[1].replace(",", ""); // Remove a vírgula do dia
        String ano = partes[2];
        
        // Convertendo o mês para o formato numérico (assumindo inglês para português)
        switch (mes) {
            case "January":
                mes = "01";
                break;
            case "February":
                mes = "02";
                break;
            case "March":
                mes = "03";
                break;
            case "April":
                mes = "04";
                break;
            case "May":
                mes = "05";
                break;
            case "June":
                mes = "06";
                break;
            case "July":
                mes = "07";
                break;
            case "August":
                mes = "08";
                break;
            case "September":
                mes = "09";
                break;
            case "October":
                mes = "10";
                break;
            case "November":
                mes = "11";
                break;
            case "December":
                mes = "12";
                break;
            default:
                // Se o mês não estiver em inglês, retornar a data original
                return dataOriginal;
        }
        
        return dia + "/" + mes + "/" + ano;
    }
    
    public byte[] toByteArray() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Codificação desejada
        Charset charset = Charset.forName("UTF-8"); // Escolha a codificação desejada

        // Convertendo char array para byte array
        byte[] byteArray = new String(type).getBytes(charset);

        dos.writeInt(id);
        dos.write(byteArray);
        dos.writeUTF(title);
        dos.writeUTF(director);
        dos.writeLong(date);

        return baos.toByteArray();
    }
    
    public void fromByteArray(byte ba[]) throws IOException{
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        id = dis.readInt();
        byte[] byteArray = new byte[7];
        dis.read(byteArray);
        type = new String(byteArray).toCharArray();
        title = dis.readUTF();
        director = dis.readUTF();
        date = dis.readLong();
    }

}
