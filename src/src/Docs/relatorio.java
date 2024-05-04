/**
 * Relatório do Projeto
 *
 * Neste projeto desenvolvemos um motor de pesquisa. Este motor de pesquisa tem as seguintes funcionalidades:
 *
 * Indexação de novos URLs, assim como os presentes nas páginas indexadas
 * Pesquisar páginas que contenham um conjunto de termos, ordenando-as por relevância (número de páginas que referenciam esta página)
 * Consultar as páginas que têm ligação para uma página específica
 * Página de administração, com informações importantes como por exemplo o estado de operacionalidade de cada um dos barrels, o tempo de resposta media dos barrels, o estado de operacionalidade dos downloaders e a lista de URLs mais pesquisados.
 *
 * O relatório descreve a solução desenvolvida para o projeto de Sistemas Digitais, incluindo detalhes sobre a arquitetura de software,
 * o funcionamento das componentes Multicast e RMI, e os testes realizados.
 *
 * Arquitetura de Software:
 * - A solução desenvolvida foi um sistema distribuído, onde múltiplas threads são utilizadas para realizar o download de páginas da web, extrair informações e enviá-las pela rede.
 * - O código é organizado em classes e métodos que encapsulam funcionalidades específicas, promovendo modularidade e reutilização.
 * - A comunicação entre componentes é realizada através de sockets, com a utilização de threads para gerenciar conexões e
 *   processamento assíncrono.
 *
 * Componente Multicast:
 *  - A componente Multicast é composta por downloaders e barrels.
 * Downloaders:
 *  A classe Downloader tem como objetivo, obter as páginas Web, dado um URL. Este URL é obtido da Queue de URLs, via TCP, usando a Porta A (8081). Após a receção do URL, o Downloader obtém o conteúdo da página.
 * Conteúdo da página:
 * -URL
 * -Título
 * -Palavras
 * -URL's para outras páginas
 *
 * Após a obtenção do conteúdo, este é enviado para o Storage Barrel, usando MULTICAST com o endereço "224.3.2.1" e com a porta 4000. É também enviado para a URL Queue usando TCP os URLs que foram encontrados na página.
 * Podem existir vários Downloaders a correr em paralelo, visto que são Threads. Cada um recebe um URL da URL Queue e processa-o, independentemente dos outros.
 *
 * Barrels:
 * A classe RMIBarrel é responsável por armazenar o conteúdo das páginas da web. Cada instância de RMIBarrel é identificada por um ID único atribuído durante sua criação. O conteúdo é mantido em um HashMap, onde cada URL da página é associado ao seu respectivo conteúdo.
 * Além de armazenar, os Storage Barrels também fornecem funcionalidades de pesquisa. Ao realizar uma pesquisa por palavras-chave, os Barrels retornam uma lista de URLs e informações relacionadas.
 * Esta lista contém apenas os URLs que contêm todos os termos pesquisados e é ordenada com base no número de URLs que referenciam cada URL encontrado.
 *
 *
 * Componente RMIClient:
 * O componente RMIClient é responsável por interagir com o sistema por meio do gateway RMI, permitindo que os usuários realizem ações de forma remota.
 * Ele fornece uma interface de linha de comando para os usuários interagirem com o sistema, incluindo funcionalidades como indexar novas URLs, pesquisar páginas da web e consultar listas de hiperlinks.
 * Ao receber entradas do usuário, o RMIClient valida e encaminha as solicitações correspondentes para o gateway RMI.
 * Ele lida com exceções relacionadas à conexão com o servidor RMI, tentando novamente em caso de falha.
 * O RMIClient apresenta um menu interativo para facilitar a interação do usuário com o sistema, proporcionando uma experiência amigável e intuitiva.
 *
 *
 * Distribuição de Tarefas:
 * - O membro Rui ficou responsável pelo desenvolvimento da classe Downloader, RMI Client e pelo ficheiro Configuration.
 * - O membro João foi encarregado de projetar e implementar a AdminPage e URLQueue.
 * - O membro Marco para além de liderar o projeto, liderou a implementação da componente RMI, tratou dos Barrels definindo e garantiu a robustez do sistema.
 *
 * Testes Realizados:
 * - Foram realizados testes para verificar o correto funcionamento dos downloaders, a precisão na extração de dados das páginas web e a confiabilidade da comunicação multicast e RMI.
 * - Tentamos garantir a independência e a capacidade de lidar com falhas em cada componente do sistema. Por exemplo, nos Downloaders, se houver dificuldade em se conectar à Queue de URLs devido à sua indisponibilidade, o sistema aguarda brevemente e tenta novamente, garantindo a continuidade das operações. O mesmo princípio se aplica ao enviar URLs para a Queue. Esse mecanismo de tratamento de falhas é estendido a todos os componentes que se comunicam por TCP.
 * - Todas as exceções encontradas são devidamente tratadas e comunicadas de forma clara através de mensagens de erro, sem impactar o cliente final. Por exemplo, no Downloader, se ocorrer uma exceção ao tentar conectar-se a um URL, o sistema gerencia o problema, enviando o URL de volta para a fila para ser processado posteriormente.
 *
 * Esta abordagem garante a resiliência do sistema em face de falhas, permitindo que os componentes continuem operando mesmo que ocorram problemas em outros. O sistema foi projetado de forma que o cliente não possa causar falhas graves, e mesmo em caso de falha de um componente, o cliente não é afetado.
 *
 *
 * | Teste                          | Resultado  |
 * |--------------------------------|------------|
 * | Teste de Download              | Pass       |
 * | Teste de Extração de Dados    | Pass       |
 * | Teste de Comunicação Multicast| Pass       |
 * | Teste de Robustez do RMI      | Pass       |
 *
 *
 * @author Marco Lucas PL2 2021219146
 * @author Rui Ribeiro PL2 2021189478
 * @author João Lopes  PL2 2020236190
 *
 * @version 1.0
 */
public class relatorio {
    // Este arquivo está vazio, pois o relatório é escrito na documentação Javadoc dos métodos e classes.
}
