# Instalador Windows

[Baixar RedeDosHorizontes-Windows-x64.exe](https://github.com/guerradiegop/Super-API-Bros/raw/refs/heads/main/installers/RedeDosHorizontes-Windows-x64.exe)

Envie somente o arquivo `.exe`. Ele instala o jogo e um runtime Java próprio no perfil do usuário, com atalhos no menu Iniciar e na área de trabalho. O destinatário não precisa instalar Java, baixar o código ou estar conectado à internet para jogar. Destinado a Windows x64; não é um executável portátil de abertura sem instalação.

O instalador não tem assinatura digital de editor. O Windows pode exibir um aviso de aplicativo desconhecido.

`SHA256SUMS.txt` permite verificar a integridade. `build-info.json` informa versão, tamanho, commit do código empacotado e validações executadas.

## Gerar novamente

No Windows com JDK 17 e WiX Toolset 3.x, execute na raiz do repositório:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/package-windows.ps1 -Version 1.0.0
```

O script compila, executa os testes, monta um runtime próprio, repete os testes nesse runtime, verifica a abertura da janela pelo launcher nativo e gera o instalador. As pastas temporárias ficam em `build/windows-package/`.

O workflow **Windows installer with bundled Java** gera e publica o instalador na `main` após mudanças no código ou no empacotamento. Também pode ser iniciado manualmente pela aba Actions. Ele não publica se a `main` mudar durante a geração. O artefato do workflow fica disponível mesmo se a publicação do commit falhar.
