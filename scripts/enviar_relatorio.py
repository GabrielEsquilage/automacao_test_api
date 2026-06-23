import smtplib
import os
import re
from email.message import EmailMessage
import getpass

def extrair_resumo_do_html(html_content):
    # Usando expressões regulares simples para buscar os contadores gerados pela ApiReport.java
    try:
        total = re.search(r'<strong>(\d+)</strong><span>Total</span>', html_content).group(1)
        passou = re.search(r'<strong>(\d+)</strong><span>Passou</span>', html_content).group(1)
        falhou = re.search(r'<strong>(\d+)</strong><span>Falhou</span>', html_content).group(1)
        tempo = re.search(r'<strong>([^<]+)</strong><span>Tempo</span>', html_content).group(1)
        ambiente = re.search(r'<span>Ambiente:\s*([^<]+)</span>', html_content).group(1)
        return total, passou, falhou, tempo, ambiente
    except Exception as e:
        return "?", "?", "?", "?", "Local/Dev"

def enviar_relatorio():
    print("=== Disparador de Relatório com Resumo e Anexo ===")
    
    report_path = 'target/api-report/index.html'
    if not os.path.exists(report_path):
        print(f"ERRO: Relatório não encontrado em {report_path}")
        print("Por favor, rode 'mvn clean test' primeiro.")
        return

    # Lê o HTML para anexar e extrair dados
    with open(report_path, 'r', encoding='utf-8') as f:
        html_content = f.read()

    total, passou, falhou, tempo, ambiente = extrair_resumo_do_html(html_content)

    remetente = input("Seu e-mail (ex: seu.nome@gmail.com): ").strip()
    senha = getpass.getpass("Sua senha (ou App Password se tiver MFA ativado): ")
    destinatario = input("E-mail de destino (quem vai receber): ").strip()

    # Criação da mensagem
    msg = EmailMessage()
    msg['Subject'] = f'[AUTOMACAO ERP] Resultado dos Testes - Ambiente {ambiente.upper()}'
    msg['From'] = remetente
    msg['To'] = destinatario

    # Monta um corpo de email em HTML MODERNO e ESTRUTURADO (seguro para Outlook)
    # Usando tabelas e CSS inline básico, que são garantidos em 100% dos clientes de e-mail.
    
    cor_passou = "#15803d" if falhou == "0" else "#333"
    cor_falhou = "#b91c1c" if falhou != "0" and falhou != "?" else "#333"
    status_geral = "✅ SUCESSO" if falhou == "0" else "❌ FALHA"
    cor_header = "#0f172a"

    corpo_email = f"""\
    <!DOCTYPE html>
    <html>
      <body style="margin: 0; padding: 20px; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f1f5f9; color: #1e293b;">
        <table align="center" width="100%" max-width="600" cellpadding="0" cellspacing="0" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); margin: 0 auto; border: 1px solid #e2e8f0;">
          
          <!-- Cabeçalho -->
          <tr>
            <td style="background-color: {cor_header}; padding: 25px 30px; text-align: center;">
              <h2 style="margin: 0; color: #ffffff; font-size: 24px; font-weight: 600;">Automacao API - ERP</h2>
              <p style="margin: 5px 0 0 0; color: #94a3b8; font-size: 14px;">Relatório de Execução de Testes</p>
            </td>
          </tr>
          
          <!-- Corpo do Resumo -->
          <tr>
            <td style="padding: 30px;">
              <p style="margin-top: 0; font-size: 16px; line-height: 1.5;">Olá equipe,</p>
              <p style="font-size: 16px; line-height: 1.5;">A última bateria de testes automatizados foi finalizada no ambiente <strong>{ambiente.upper()}</strong> com o status geral de <strong>{status_geral}</strong>.</p>
              
              <!-- Cards de Estatísticas (Tabela) -->
              <table width="100%" cellpadding="0" cellspacing="0" style="margin: 25px 0;">
                <tr>
                  <!-- Total -->
                  <td width="50%" style="padding: 10px;">
                    <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 15px; text-align: center;">
                      <span style="display: block; font-size: 12px; color: #64748b; text-transform: uppercase; font-weight: bold;">Total de Testes</span>
                      <span style="display: block; font-size: 28px; font-weight: bold; color: #0f172a; margin-top: 5px;">{total}</span>
                    </div>
                  </td>
                  <!-- Tempo -->
                  <td width="50%" style="padding: 10px;">
                    <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 15px; text-align: center;">
                      <span style="display: block; font-size: 12px; color: #64748b; text-transform: uppercase; font-weight: bold;">Tempo de Execução</span>
                      <span style="display: block; font-size: 28px; font-weight: bold; color: #0f172a; margin-top: 5px;">{tempo}</span>
                    </div>
                  </td>
                </tr>
                <tr>
                  <!-- Passaram -->
                  <td width="50%" style="padding: 10px;">
                    <div style="background-color: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 6px; padding: 15px; text-align: center;">
                      <span style="display: block; font-size: 12px; color: #166534; text-transform: uppercase; font-weight: bold;">Passaram</span>
                      <span style="display: block; font-size: 28px; font-weight: bold; color: {cor_passou}; margin-top: 5px;">{passou}</span>
                    </div>
                  </td>
                  <!-- Falharam -->
                  <td width="50%" style="padding: 10px;">
                    <div style="background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 6px; padding: 15px; text-align: center;">
                      <span style="display: block; font-size: 12px; color: #991b1b; text-transform: uppercase; font-weight: bold;">Falharam</span>
                      <span style="display: block; font-size: 28px; font-weight: bold; color: {cor_falhou}; margin-top: 5px;">{falhou}</span>
                    </div>
                  </td>
                </tr>
              </table>
              
              <div style="background-color: #fff8f1; border-left: 4px solid #f97316; padding: 15px; margin-top: 10px;">
                <p style="margin: 0; font-size: 14px; color: #9a3412;">
                  <strong>Atenção:</strong> O relatório técnico completo, contendo todos os detalhes de <i>Request</i> e <i>Response</i> de cada endpoint, encontra-se <b>anexado a este e-mail</b>. 
                  Faça o download do arquivo <code>relatorio_testes_api.html</code> e abra no seu navegador preferido.
                </p>
              </div>
              
            </td>
          </tr>
          
          <!-- Rodapé -->
          <tr>
            <td style="background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;">
              <p style="margin: 0; font-size: 12px; color: #64748b;">Este é um e-mail automático gerado pela automação de testes.</p>
            </td>
          </tr>
        </table>
      </body>
    </html>
    """
    
    msg.set_content("Por favor, ative a visualização em HTML do seu cliente de e-mail para ver o resumo.")
    msg.add_alternative(corpo_email, subtype='html')

    # Anexa o arquivo HTML original
    # Usando application/octet-stream ou text/html como anexo
    with open(report_path, 'rb') as f:
        anexo_dados = f.read()
        
    msg.add_attachment(anexo_dados, maintype='text', subtype='html', filename='relatorio_testes_api.html')

    print("\nConectando ao servidor SMTP...")
    try:
        # Configuração para Gmail
        with smtplib.SMTP_SSL('smtp.gmail.com', 465) as smtp:
            smtp.login(remetente, senha)
            print("Autenticado com sucesso! Enviando e-mail...")
            smtp.send_message(msg)
            print("✅ E-mail enviado com sucesso com o resumo no corpo e o relatório em anexo!")
    except Exception as e:
        print(f"❌ Ocorreu um erro ao enviar: {e}")

if __name__ == '__main__':
    enviar_relatorio()
