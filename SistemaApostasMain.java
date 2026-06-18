import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

class GerenciadorBD {
    private static final String URL = "jdbc:sqlite:campeonato_apostas.db";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void inicializarBanco() {
        String sqlTimes = "CREATE TABLE IF NOT EXISTS times (nome TEXT PRIMARY KEY);";
        String sqlParticipantes = "CREATE TABLE IF NOT EXISTS participantes (nome TEXT PRIMARY KEY, pontuacao INTEGER);";
        
        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlTimes);
            stmt.execute(sqlParticipantes);
            System.out.println("[BD] Tabelas verificadas/criadas com sucesso.");
        } catch (SQLException e) {
            System.out.println("[BD] Erro ao inicializar tabelas: " + e.getMessage());
        }
    }

    public static void salvarTime(String nome) {
        String sql = "INSERT OR IGNORE INTO times(nome) VALUES(?)";
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[BD] Erro ao salvar time: " + e.getMessage());
        }
    }

    public static void salvarParticipante(String nome, int pontuacao) {
        String sql = "INSERT OR REPLACE INTO participantes(nome, pontuacao) VALUES(?, ?)";
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.setInt(2, pontuacao);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[BD] Erro ao salvar participante: " + e.getMessage());
        }
    }

    public static List<String> listarTimes() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nome FROM times";
        try (Connection conn = conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rs.getString("nome"));
            }
        } catch (SQLException e) {
            System.out.println("[BD] Erro ao listar times: " + e.getMessage());
        }
        return lista;
    }
}

abstract class Usuario {
    protected String nome; 
    public Usuario(String nome) { this.nome = nome; }
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
    public void adicionarPoints(int pontos) { 
        this.pontuacao += pontos; 
        GerenciadorBD.salvarParticipante(this.nome, this.pontuacao);
    }

    @Override
    public void exibirDetalhes() {
        System.out.println("Participante: " + nome + " | Pontos: " + pontuacao);
    }
}

class Time {
    private String nome;
    public Time(String nome) { 
        this.nome = nome; 
        GerenciadorBD.salvarTime(nome);
    }
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
        class Adicionar { }
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
            participante.adicionarPoints(10); 
        } else if (acertouResultado) {
            participante.adicionarPoints(5); 
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
            GerenciadorBD.salvarParticipante(p.getNome(), p.getPontuacao());
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
        GerenciadorBD.inicializarBanco();

        Time arsenal = new Time("Arsenal");
        Time barcelona = new Time("Barcelona");

        System.out.println("\n[BD] Times atualmente salvos no Banco de Dados:");
        for(String nomeTime : GerenciadorBD.listarTimes()) {
            System.out.println("- " + nomeTime);
        }

        Partida jogo1 = new Partida(arsenal, barcelona, LocalDateTime.now().plusDays(1));

        GrupoApostas grupoFirma = new GrupoApostas("Liga dos Amigos");
        Participante p1 = new Participante("Marcus");
        Participante p2 = new Participante("João");
        
        grupoFirma.adicionarParticipante(p1);
        grupoFirma.adicionarParticipante(p2);

        Aposta apostaMarcus = new Aposta(p1, jogo1, 2, 1); 
        Aposta apostaJoao = new Aposta(p2, jogo1, 1, 0);   

        if(apostaMarcus.apostaValidaTempo() && apostaJoao.apostaValidaTempo()) {
            System.out.println("\nApostas registradas com sucesso!");
        }

        jogo1.setResultadoReal(2, 1);

        apostaMarcus.calcularPontuacao(); 
        apostaJoao.calcularPontuacao(); 

        grupoFirma.exibirClassificacao();
    }
}
