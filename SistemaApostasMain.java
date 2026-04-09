import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

abstract class Usuario {
    protected String nome; 
    public Usuario(String nome) {
        this.nome = nome;
    }

    public String getNome() { return nome; }
    public abstract void exibirDetalhes(); 
}

class Participante extends Usuario {
    private int pontuacao;
    public Participante(String nome) {
        super(nome);
        this.pontuacao = 0;
    }

    public int getPontuacao() { return pontuacao; }
    public void adicionarPontos(int pontos) { this.pontuacao += pontos; }

    @Override
    public void exibirDetalhes() {
        System.out.println("Participante: " + nome + " | Pontos: " + pontuacao);
    }
}

class Time {
    private String nome;
    public Time(String nome) { this.nome = nome; }
    public String getNome() { return nome; }
}

class Partida {
    private Time timeCasa;
    private Time timeFora;
    private LocalDateTime dataHorario;
    private int golsCasaReal = -1;
    private int golsForaReal = -1;
    private boolean finalizada = false;

    public Partida(Time timeCasa, Time timeFora, LocalDateTime dataHorario) {
        this.timeCasa = timeCasa;
        this.timeFora = timeFora;
        this.dataHorario = dataHorario;
    }

    public void setResultadoReal(int golsCasa, int golsFora) {
        this.golsCasaReal = golsCasa;
        this.golsForaReal = golsFora;
        this.finalizada = true;
    }

    public boolean isFinalizada() { return finalizada; }
    public int getGolsCasaReal() { return golsCasaReal; }
    public int getGolsForaReal() { return golsForaReal; }
    public LocalDateTime getDataHorario() { return dataHorario; }
    public Time getTimeCasa() { return timeCasa; }
    public Time getTimeFora() { return timeFora; }
}

class Aposta {
    private Participante participante;
    private Partida partida;
    private int golsCasaAposta;
    private int golsForaAposta;
    private LocalDateTime dataAposta;

    public Aposta(Participante participante, Partida partida, int golsCasa, int golsFora) {
        this.participante = participante;
        this.partida = partida;
        this.golsCasaAposta = golsCasa;
        this.golsForaAposta = golsFora;
        this.dataAposta = LocalDateTime.now();
    }

    public Aposta() {
        this.dataAposta = LocalDateTime.now();
    }

    public void calcularPontuacao() {
        if (!partida.isFinalizada()) return;

        boolean acertouPlacarExato = (golsCasaAposta == partida.getGolsCasaReal()) && 
                                     (golsForaAposta == partida.getGolsForaReal());
        
        boolean realEmpate = partida.getGolsCasaReal() == partida.getGolsForaReal();
        boolean apostaEmpate = golsCasaAposta == golsForaAposta;
        boolean realCasaVenceu = partida.getGolsCasaReal() > partida.getGolsForaReal();
        boolean apostaCasaVenceu = golsCasaAposta > golsForaAposta;
        boolean realForaVenceu = partida.getGolsCasaReal() < partida.getGolsForaReal();
        boolean apostaForaVenceu = golsCasaAposta < golsForaAposta;

        boolean acertouResultado = (realEmpate && apostaEmpate) || 
                                   (realCasaVenceu && apostaCasaVenceu) || 
                                   (realForaVenceu && apostaForaVenceu);

        if (acertouPlacarExato) {
            participante.adicionarPontos(10); 
        } else if (acertouResultado) {
            participante.adicionarPontos(5); 
        }
    }

    public boolean apostaValidaTempo() {
        long minutosDiferenca = ChronoUnit.MINUTES.between(this.dataAposta, partida.getDataHorario());
        return minutosDiferenca >= 20;
    }
}

class GrupoApostas {
    private String nome;
    private List<Participante> participantes = new ArrayList<>();
    public GrupoApostas(String nome) { this.nome = nome; }

    public void adicionarParticipante(Participante p) {
        if (participantes.size() < 5) { 
            participantes.add(p);
        } else {
            System.out.println("Grupo cheio! Máximo de 5 participantes.");
        }
    }

    public void exibirClassificacao() {
        System.out.println("\n--- Classificação do Grupo: " + nome + " ---");
        participantes.sort((p1, p2) -> Integer.compare(p2.getPontuacao(), p1.getPontuacao()));
        for (Participante p : participantes) {
            p.exibirDetalhes(); 
        }
    }
}

public class SistemaApostasMain {
    public static void main(String[] args) {
        Time arsenal = new Time("Arsenal");
        Time barcelona = new Time("Barcelona");

        Partida jogo1 = new Partida(arsenal, barcelona, LocalDateTime.now().plusDays(1));

        GrupoApostas grupoFirma = new GrupoApostas("Liga dos Amigos");
        Participante p1 = new Participante("Marcus");
        Participante p2 = new Participante("João");
        
        grupoFirma.adicionarParticipante(p1);
        grupoFirma.adicionarParticipante(p2);

        Aposta apostaMarcus = new Aposta(p1, jogo1, 2, 1); 
        Aposta apostaJoao = new Aposta(p2, jogo1, 1, 0);   

        if(apostaMarcus.apostaValidaTempo() && apostaJoao.apostaValidaTempo()) {
            System.out.println("Apostas registradas com sucesso!");
        }

        jogo1.setResultadoReal(2, 1);

        apostaMarcus.calcularPontuacao(); 
        apostaJoao.calcularPontuacao(); 

        grupoFirma.exibirClassificacao();
    }
}
