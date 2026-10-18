# Alternar DNS

App Android com um bloco nas Configurações rápidas que alterna o DNS particular entre **Desativado** e `dns.adguard-dns.com` com um toque.

## Compilar

**Android Studio:** abra a pasta do projeto e use Build > Build APK(s).

**Sem computador local (GitHub Actions):** envie esta pasta para um repositório no GitHub. O workflow em `.github/workflows/build.yml` gera o APK, que fica disponível na aba Actions, em Artifacts.

## Instalar e configurar

1. Instale o APK no celular.
2. Uma única vez, com o celular no computador via USB e a depuração USB ativada, execute:

   ```
   adb shell pm grant com.example.dnsswitch android.permission.WRITE_SECURE_SETTINGS
   ```

3. Abra as Configurações rápidas, toque no lápis e arraste o bloco **DNS AdGuard** para o painel.

Para usar outro servidor, altere `HOST` em `DnsController.kt`.
