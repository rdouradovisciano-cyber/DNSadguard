# Alternar DNS

App Android com um bloco nas Configurações rápidas que alterna o DNS particular entre **Desativado** e `dns.adguard-dns.com` com um toque. Também desativa o DNS automaticamente ao conectar em Wi-Fi específicos.

## Compilar

**Android Studio:** abra a pasta do projeto e use Build > Build APK(s).

**Sem computador local (GitHub Actions):** o workflow em `.github/workflows/build.yml` gera o APK a cada envio, disponível na aba Actions, em Artifacts.

## Instalar e configurar

1. Instale o APK no celular.
2. Uma única vez, com o celular no computador via USB e a depuração USB ativada, execute:

   ```
   adb shell pm grant com.example.dnsswitch android.permission.WRITE_SECURE_SETTINGS
   ```

3. Abra as Configurações rápidas, toque no lápis e arraste o bloco **DNS AdGuard** para o painel.

## Desativar em Wi-Fi específico

Para o monitor ler o nome da rede, conceda também estas permissões (o app mostra os comandos que faltam):

```
adb shell pm grant com.example.dnsswitch android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.example.dnsswitch android.permission.ACCESS_BACKGROUND_LOCATION
adb shell pm grant com.example.dnsswitch android.permission.POST_NOTIFICATIONS
```

Depois, no app: conecte-se à rede, toque em **Adicionar rede Wi-Fi atual** e ative **Monitoramento**. A Localização do celular precisa estar ligada, só para o Android liberar o nome da rede.

Ao entrar numa rede da lista, o DNS é desativado (se o AdGuard estava ligado). Ao sair dela, volta ao estado anterior.

## Observações

- O APK é assinado com a chave `app/debug.keystore`, que fica no repositório de propósito: assim as atualizações instalam por cima sem perder as permissões. Serve para uso pessoal; não use para publicar na loja.
- Para usar outro servidor, altere `HOST` em `DnsController.kt`.
