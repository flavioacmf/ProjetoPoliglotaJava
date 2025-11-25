public class Noticia {
    // Atributos da classe
    private String id;
    private String titulo;
    private String conteudoCompleto; // Este campo será armazenado principalmente no MongoDB
    private String tags;

    // Construtor vazio (necessário para algumas bibliotecas de serialização/deserialização como Jackson)
    public Noticia() {
    }

    // Construtor com argumentos para facilitar a criação de novos objetos
    public Noticia(String titulo, String conteudoCompleto, String tags) {
        this.titulo = titulo;
        this.conteudoCompleto = conteudoCompleto;
        this.tags = tags;
    }

    // --- Getters e Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudoCompleto() {
        return conteudoCompleto;
    }

    public void setConteudoCompleto(String conteudoCompleto) {
        this.conteudoCompleto = conteudoCompleto;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // Método opcional para imprimir o objeto de forma legível no console
    @Override
    public String toString() {
        return "Noticia{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", tags='" + tags + '\'' +
                '}'; // Não incluí o conteudoCompleto para não poluir o log
    }
}