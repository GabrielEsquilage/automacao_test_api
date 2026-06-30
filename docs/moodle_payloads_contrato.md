# Documentação de Contratos: API Moodle

Este documento mapeia as funções consumidas pelo ERP na integração com o Moodle, detalhando os parâmetros obrigatórios e os formatos de payload esperados (tanto para as funções nativas do núcleo do Moodle quanto para os scripts PHP customizados).

> [!NOTE]
> O Moodle REST API tradicionalmente não utiliza payloads em formato JSON raw (`application/json`) no `body`. Ele consome os dados em formato **form-urlencoded** (`application/x-www-form-urlencoded`), serializando arrays diretamente na URL ou no corpo da requisição POST (exemplo: `users[0][username]=teste`).

---

## 1. Funções Nativas do Moodle (Core)

Todas as requisições nativas devem ser enviadas para o endpoint base:
`POST /webservice/rest/server.php?moodlewsrestformat=json&wstoken={TOKEN}`

### `core_webservice_get_site_info`
*   **Descrição:** Retorna as informações do site, do usuário dono do token e as funções liberadas.
*   **Payload Esperado:** *(Nenhum. Apenas os query params padrão)*

### `core_user_create_users`
*   **Descrição:** Criação em lote (ou individual) de usuários no Moodle.
*   **Parâmetros Obrigatórios (Array `users`):**
    *   `users[0][username]` (string)
    *   `users[0][password]` (string)
    *   `users[0][firstname]` (string)
    *   `users[0][lastname]` (string)
    *   `users[0][email]` (string)

### `core_user_get_users_by_field`
*   **Descrição:** Busca usuários com base em um campo específico (ex: `username`, `email`, `idnumber`).
*   **Parâmetros Obrigatórios:**
    *   `field` (string) - O campo pelo qual pesquisar.
    *   `values[0]` (string) - O valor pesquisado.

### `core_course_create_categories`
*   **Descrição:** Criação de categorias de curso.
*   **Parâmetros Obrigatórios (Array `categories`):**
    *   `categories[0][name]` (string)
    *   `categories[0][parent]` (int) - ID da categoria pai (0 para raiz).

### `core_course_duplicate_course`
*   **Descrição:** Duplica um curso existente (usado para provisionamento rápido).
*   **Parâmetros Obrigatórios:**
    *   `courseid` (int) - ID do curso a ser clonado.
    *   `fullname` (string) - Nome completo do novo curso.
    *   `shortname` (string) - Nome breve/código do novo curso.
    *   `categoryid` (int) - ID da categoria onde o curso será salvo.

### `core_course_get_courses_by_field`
*   **Descrição:** Busca cursos com base em um campo específico.
*   **Parâmetros Obrigatórios:**
    *   `field` (string) - Ex: `idnumber` ou `shortname`.
    *   `value` (string) - O valor pesquisado.

### `enrol_manual_enrol_users`
*   **Descrição:** Realiza a matrícula manual de alunos em um curso/disciplina.
*   **Parâmetros Obrigatórios (Array `enrolments`):**
    *   `enrolments[0][roleid]` (int) - Ex: 5 (Estudante).
    *   `enrolments[0][userid]` (int) - ID do usuário no Moodle.
    *   `enrolments[0][courseid]` (int) - ID do curso no Moodle.

### `core_enrol_get_users_courses`
*   **Descrição:** Lista todos os cursos nos quais um usuário está matriculado.
*   **Parâmetros Obrigatórios:**
    *   `userid` (int) - ID numérico do usuário.

### `gradereport_user_get_grade_items`
*   **Descrição:** Recupera o boletim/quadro de notas de um aluno em um curso.
*   **Parâmetros Obrigatórios:**
    *   `courseid` (int) - ID do curso.
    *   `userid` (int) - *Opcional* (Se não enviado, retorna de todos os alunos).

---

## 2. Scripts PHP Customizados (Pasta `/arq/disponivel/`)

Esses scripts foram desenvolvidos especificamente para a regra de negócio da instituição. Eles não passam pelo dispatcher nativo do webservice (`server.php`), mas são acessados diretamente na rota raiz e possuem validações de parâmetros mais rígidas, muitas vezes retornando JSONs dedicados de erro.

> [!WARNING]
> Apesar de receberem o `wstoken` na URL, alguns destes scripts acusam "Token de segurança inválido" caso não recebam as chaves adicionais de autorização parametrizadas internamente no PHP.

### `oculta_atividade.php` e `oculta_rotulo.php`
*   **Descrição:** Define a visibilidade de uma atividade ou rótulo específico.
*   **Método:** POST
*   **Parâmetros Obrigatórios (Mapeados via captura de erro):**
    *   `disciplina` (int) - O ID do curso/disciplina.
    *   `atividade` (int) - O ID do módulo/atividade a ser ocultado.
    *   `visivel` (int/boolean) - Status de visibilidade (ex: 0 ou 1).
    *   `token` (string) - Token de segurança adicional.

### `atualiza_status_disciplina.php`
*   **Descrição:** Atualiza o status geral do course.
*   **Método:** POST
*   **Parâmetros Obrigatórios (Presumidos pela documentação de erro "Parâmetros incompletos"):**
    *   `disciplina` (int)
    *   `status` (int/string)
    *   `token` (string)

### `atualiza_data_atividades.php` e `atualiza_data_inicio_fim_atividade.php`
*   **Descrição:** Ajusta cronogramas de datas para atividades do curso.
*   **Método:** POST
*   **Parâmetros Obrigatórios Esperados:**
    *   `disciplina` (int)
    *   `atividade` (int) - *Apenas para a rota de início/fim específico*
    *   `data_inicio` (timestamp/string)
    *   `data_fim` (timestamp/string)
    *   `token` (string)

### `sobrepoe_atualiza_data_atividade_tarefa.php`
*   **Descrição:** Cria exceções de datas (prorrogações) para atividades específicas.
*   **Método:** POST
*   **Parâmetros Obrigatórios Esperados:**
    *   `disciplina` (int)
    *   `atividade` (int)
    *   `userid` (int) - ID do usuário beneficiado pela prorrogação.
    *   `nova_data` (timestamp/string)

### `integra_sso.php`
*   **Descrição:** Link de acesso (Single Sign-On) para direcionar o aluno logado do ERP para o course.
*   **Método:** GET / POST
*   **Parâmetros Obrigatórios Esperados:**
    *   Chaves de criptografia/SSO de sessão (Geralmente validam cookie/sessão, por isso retorna "Acesso restrito!" quando chamado via API pura sem header de auth de usuário).

### `retorna_agenda_url.php`
*   **Descrição:** Retorna os eventos de calendário.
*   **Método:** GET
*   **Parâmetros Obrigatórios Esperados:**
    *   Pode requerer o identificador do aluno/curso, e explicitamente exige o parâmetro `token` interno validado, além do próprio `wstoken`.
