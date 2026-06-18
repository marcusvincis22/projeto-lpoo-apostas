# projeto-lpoo-apostas

---

## Atualização: Banco de Dados e Padrão DAO

Para a segunda parte do trabalho, estruturei a persistência usando o **SQLite**, que gera o banco local automaticamente em um arquivo `.db` assim que o programa roda. Para organizar o acesso aos dados, apliquei o padrão de projeto **DAO** na classe `GerenciadorBD`.

### Como rodar:
Como o projeto usa o SQLite local, é necessário garantir que o driver JDBC do SQLite (`sqlite-jdbc`) esteja adicionado nas bibliotecas/classpath da ide na hora de executar o código principal.
