package io.github.ecotrip.aws;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.github.ecotrip.Generated;
import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.auth.credentials.X509CredentialsProvider;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.crt.mqtt.*;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

/**
 * Command line utils
 */
@Generated
public class CommandLineUtils {
    // Constants for commonly used/needed commands
    private static final String m_cmd_ca_file = "ca_file";
    private static final String m_cmd_cert_file = "cert";
    private static final String m_cmd_endpoint = "endpoint";
    private static final String m_cmd_key_file = "key";
    private static final String m_cmd_proxy_host = "proxy_host";
    private static final String m_cmd_proxy_port = "proxy_port";
    private static final String m_cmd_signing_region = "signing_region";
    private static final String m_cmd_x509_endpoint = "x509_endpoint";
    private static final String m_cmd_x509_role = "x509_role_alias";
    private static final String m_cmd_x509_thing_name = "x509_thing_name";
    private static final String m_cmd_x509_cert_file = "x509_cert";
    private static final String m_cmd_x509_key_file = "x509_key";
    private static final String m_cmd_x509_ca_file = "x509_ca_file";
    private static final String m_cmd_pkcs11_lib = "pkcs11_lib";
    private static final String m_cmd_pkcs11_cert = "cert";
    private static final String m_cmd_pkcs11_pin = "ppin";
    private static final String m_cmd_pkcs11_token = "token_label";
    private static final String m_cmd_pkcs11_slot = "slot_id";
    private static final String m_cmd_pkcs11_key = "key_label";
    private static final String m_cmd_message = "message";
    private static final String m_cmd_topic = "topic";
    private static final String m_cmd_help = "help";
    private static final String m_cmd_custom_auth_username = "custom_auth_username";
    private static final String m_cmd_custom_auth_authorizer_name = "custom_auth_authorizer_name";
    private static final String m_cmd_custom_auth_authorizer_signature = "custom_auth_authorizer_signature";
    private static final String m_cmd_custom_auth_password = "custom_auth_password";

    private String programName;
    private final HashMap<String, CommandLineOption> registeredCommands = new HashMap<>();
    private List<String> commandArguments;

    public void registerProgramName(String newProgramName) {
        programName = newProgramName;
    }

    /**
     * @param option CommandLineOption
     */
    public void registerCommand(CommandLineOption option) {
        if (registeredCommands.containsKey(option.getCommandName())) {
            System.out.println("Cannot register command: " + option.getCommandName() + ". Command already registered");
            return;
        }
        registeredCommands.put(option.getCommandName(), option);
    }

    public void registerCommand(String commandName, String exampleInput, String helpOutput) {
        registerCommand(new CommandLineOption(commandName, exampleInput, helpOutput));
    }

    public void removeCommand(String commandName) {
        registeredCommands.remove(commandName);
    }

    /**
     * @param commandName
     * @param newCommandHelp
     */
    public void updateCommandHelp(String commandName, String newCommandHelp) {
        if (registeredCommands.containsKey(commandName)) {
            registeredCommands.get(commandName).setHelpOutput(newCommandHelp);
        }
    }

    /**
     * @param arguments
     */
    public void sendArguments(String[] arguments) {
        // Automatically register the help command
        registerCommand(m_cmd_help, "", "Prints this message");

        commandArguments = Arrays.asList(arguments);

        // Automatically check for help and print if present
        if (hasCommand(m_cmd_help)) {
            printHelp();
            System.exit(-1);
        }
    }

    public boolean hasCommand(String command) {
        return commandArguments.contains("--" + command);
    }

    public String getCommand(String command) {
        for (Iterator<String> iter = commandArguments.iterator(); iter.hasNext();) {
            String value = iter.next();
            if (Objects.equals(value, "--" + command)) {
                if (iter.hasNext()) {
                    return iter.next();
                } else {
                    System.out.println("Error - found command but at end of arguments!\n");
                    return "";
                }
            }
        }
        return "";
    }

    public String getCommandOrDefault(String command, String commandDefault) {
        if (commandArguments.contains("--" + command)) {
            return getCommand(command);
        }
        return commandDefault;
    }

    public String getCommandRequired(String command, String optionalAdditionalMessage) {
        if (commandArguments.contains("--" + command)) {
            return getCommand(command);
        }
        printHelp();
        System.out.println("Missing required argument: --" + command + "\n");
        if (!Objects.equals(optionalAdditionalMessage, "")) {
            System.out.println(optionalAdditionalMessage + "\n");
        }
        System.exit(-1);
        return "";
    }

    /**
     *  print command line help
     */
    public void printHelp() {
        System.out.println("Usage:");

        String messageOne = programName;
        for (String commandName : registeredCommands.keySet()) {
            messageOne += " --" + commandName + " " + registeredCommands.get(commandName).getExampleInput();
        }
        System.out.println(messageOne + "\n");

        for (String commandName : registeredCommands.keySet()) {
            messageOne += " --" + commandName + " " + registeredCommands.get(commandName).getExampleInput();
            System.out.println("* " + commandName + "\t\t" + registeredCommands.get(commandName).getHelpOutput());
        }
    }

    /**
     *  register base MQTT commands
     */
    public void addCommonMqttCommands() {
        registerCommand(m_cmd_endpoint, "<str>", "The endpoint of the mqtt server, not including a port.");
        registerCommand(m_cmd_ca_file, "<path>",
            "Path to AmazonRootCA1.pem (optional, system trust store used by default).");
        registerCommand("verbosity", "<str>", "The amount of detail in the logging output of the sample."
                + " Options: 'fatal', 'error', 'warn', 'info', 'debug', 'trace' or 'none' (optional, default='none').");
    }

    /**
     *  register base proxy commands
     */
    public void addCommonProxyCommands() {
        registerCommand(m_cmd_proxy_host, "<str>",
                "Websocket proxy host to use (optional, required if --proxy_port is set).");
        registerCommand(m_cmd_proxy_port, "<int>",
            "Websocket proxy port to use (optional, default=8080, required if --proxy_host is set).");
    }

    /**
     *  register X509 commands
     */
    public void addCommonX509Commands() {
        registerCommand(
                m_cmd_x509_role, "<str>", "Role alias to use with the x509 credentials provider (required for x509)");
        registerCommand(m_cmd_x509_endpoint, "<str>", "Endpoint to fetch x509 credentials from (required for x509)");
        registerCommand(
                m_cmd_x509_thing_name, "<str>",
                "Thing name to fetch x509 credentials on behalf of (required for x509)");
        registerCommand(
                m_cmd_x509_cert_file,
                "<path>",
                "Path to the IoT thing certificate used in fetching x509 credentials (required for x509)");
        registerCommand(
                m_cmd_x509_key_file,
                "<path>",
                "Path to the IoT thing private key used in fetching x509 credentials (required for x509)");
        registerCommand(
                m_cmd_x509_ca_file,
                "<path>",
                "Path to the root certificate used in fetching x509 credentials (required for x509)");
    }

    /**
     *  register common topic commands
     */
    public void addCommonTopicMessageCommands() {
        registerCommand(m_cmd_message, "<str>",
            "The message to send in the payload (optional, default='Hello world!')");
        registerCommand(m_cmd_topic, "<str>", "Topic to publish, subscribe to. (optional, default='test/topic')");
    }

    /**
     *  register common topic commands
     */
    public MqttClientConnection buildCustomKeyOperationConnection(
            MqttClientConnectionEvents callbacks, TlsContextCustomKeyOperationOptions customKeyOperationOptions) {
        try {
            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsCustomKeyOperationsBuilder(
                    customKeyOperationOptions);
            buildConnectionSetupCaFileDefaults(builder);
            buildConnectionSetupConnectionDefaults(builder, callbacks);
            buildConnectionSetupProxyDefaults(builder);

            MqttClientConnection conn = builder.build();
            builder.close();
            return conn;

        } catch (CrtRuntimeException ex) {
            return null;
        }
    }

    /**
     *  build PKCS11 MQTT connection
     */
    public MqttClientConnection buildPkcs11MqttConnection(MqttClientConnectionEvents callbacks) {
        try {

            Pkcs11Lib pkcs11Lib = new Pkcs11Lib(getCommandRequired(m_cmd_pkcs11_lib, ""));
            TlsContextPkcs11Options pkcs11Options = new TlsContextPkcs11Options(pkcs11Lib);

            pkcs11Options.withCertificateFilePath(getCommandRequired(m_cmd_cert_file, ""));
            pkcs11Options.withUserPin(getCommandRequired(m_cmd_pkcs11_pin, ""));

            if (hasCommand(m_cmd_pkcs11_token)) {
                pkcs11Options.withTokenLabel(getCommand(m_cmd_pkcs11_token));
            }

            if (hasCommand(m_cmd_pkcs11_slot)) {
                pkcs11Options.withSlotId(Long.parseLong(getCommand(m_cmd_pkcs11_slot)));
            }

            if (hasCommand(m_cmd_pkcs11_key)) {
                pkcs11Options.withPrivateKeyObjectLabel(getCommand(m_cmd_pkcs11_key));
            }

            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsPkcs11Builder(pkcs11Options);

            buildConnectionSetupCaFileDefaults(builder);
            buildConnectionSetupConnectionDefaults(builder, callbacks);

            MqttClientConnection conn = builder.build();
            builder.close();
            return conn;

        } catch (CrtRuntimeException ex) {
            return null;
        }
    }

    /**
     *  build X509 MQTT connection
     */
    public MqttClientConnection buildWebsocketX509MqttConnection(MqttClientConnectionEvents callbacks) {
        try {

            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(null, null);
            buildConnectionSetupCaFileDefaults(builder);
            buildConnectionSetupConnectionDefaults(builder, callbacks);

            HttpProxyOptions proxyOptions = null;
            int proxyPort = Integer.parseInt(getCommandOrDefault(m_cmd_proxy_port, "0"));
            if (hasCommand(m_cmd_proxy_host) && proxyPort > 0) {
                proxyOptions = new HttpProxyOptions();
                proxyOptions.setHost(getCommand(m_cmd_proxy_host));
                proxyOptions.setPort(proxyPort);
                builder.withHttpProxyOptions(proxyOptions);
            }

            builder.withWebsockets(true);
            builder.withWebsocketSigningRegion(getCommandRequired(m_cmd_signing_region, ""));

            TlsContextOptions x509TlsOptions = TlsContextOptions.createWithMtlsFromPath(
                getCommandRequired(m_cmd_x509_cert_file, ""), getCommandRequired(m_cmd_x509_key_file, ""));
            if (hasCommand(m_cmd_x509_ca_file)) {
                x509TlsOptions.withCertificateAuthorityFromPath(null, getCommand(m_cmd_x509_ca_file));
            }

            ClientTlsContext x509TlsContext = new ClientTlsContext(x509TlsOptions);
            X509CredentialsProvider.X509CredentialsProviderBuilder x509builder =
                new X509CredentialsProvider.X509CredentialsProviderBuilder()
                    .withTlsContext(x509TlsContext)
                    .withEndpoint(getCommandRequired(m_cmd_x509_endpoint, ""))
                    .withRoleAlias(getCommandRequired(m_cmd_x509_role, ""))
                    .withThingName(getCommandRequired(m_cmd_x509_thing_name, ""))
                    .withProxyOptions(proxyOptions);
            X509CredentialsProvider provider = x509builder.build();
            builder.withWebsocketCredentialsProvider(provider);

            MqttClientConnection conn = builder.build();
            builder.close();
            return conn;

        } catch (CrtRuntimeException ex) {
            return null;
        }
    }

    /**
     *  build websocket MQTT connection
     */
    public MqttClientConnection buildWebsocketMqttConnection(MqttClientConnectionEvents callbacks) {
        try {

            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(null, null);
            buildConnectionSetupCaFileDefaults(builder);
            buildConnectionSetupConnectionDefaults(builder, callbacks);
            buildConnectionSetupProxyDefaults(builder);

            builder.withWebsockets(true);
            builder.withWebsocketSigningRegion(getCommandRequired(m_cmd_signing_region, ""));

            MqttClientConnection conn = builder.build();
            builder.close();
            return conn;

        } catch (CrtRuntimeException ex) {
            return null;
        }
    }

    /**
     *  build direct MQTT connection
     */
    public MqttClientConnection buildDirectMqttConnection(MqttClientConnectionEvents callbacks) {
        try {

            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(
                    getCommandRequired(m_cmd_cert_file, ""), getCommandRequired(m_cmd_key_file, ""));
            buildConnectionSetupCaFileDefaults(builder);
            buildConnectionSetupConnectionDefaults(builder, callbacks);
            buildConnectionSetupProxyDefaults(builder);

            MqttClientConnection conn = builder.build();
            builder.close();
            return conn;
        } catch (CrtRuntimeException ex) {
            return null;
        }
    }

    /**
     *  build direct MQTT connection with auth
     */
    public MqttClientConnection buildDirectMqttConnectionWithCustomAuthorizer(MqttClientConnectionEvents callbacks) {
        try {
            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newDefaultBuilder();
            buildConnectionSetupCaFileDefaults(builder);
            buildConnectionSetupConnectionDefaults(builder, callbacks);
            builder.withCustomAuthorizer(
                    getCommandOrDefault(m_cmd_custom_auth_username, null),
                    getCommandOrDefault(m_cmd_custom_auth_authorizer_name, null),
                    getCommandOrDefault(m_cmd_custom_auth_authorizer_signature, null),
                    getCommandOrDefault(m_cmd_custom_auth_password, null));

            MqttClientConnection conn = builder.build();
            builder.close();
            return conn;
        } catch (CrtRuntimeException | UnsupportedEncodingException ex) {
            return null;
        }
    }

    private void buildConnectionSetupCaFileDefaults(AwsIotMqttConnectionBuilder builder) {
        if (hasCommand(m_cmd_ca_file)) {
            builder.withCertificateAuthorityFromPath(null, getCommand(m_cmd_ca_file));
        }
    }
    private void buildConnectionSetupConnectionDefaults(
        AwsIotMqttConnectionBuilder builder, MqttClientConnectionEvents callbacks) {
        builder.withConnectionEventCallbacks(callbacks)
                .withClientId(getCommandOrDefault("client_id", "test-" + UUID.randomUUID().toString()))
                .withEndpoint(getCommandRequired(m_cmd_endpoint, ""))
                .withPort((short) Integer.parseInt(getCommandOrDefault("port", "8883")))
                .withCleanSession(true)
                .withProtocolOperationTimeoutMs(60000);
    }
    private void buildConnectionSetupProxyDefaults(AwsIotMqttConnectionBuilder builder) {
        int proxyPort = Integer.parseInt(getCommandOrDefault(m_cmd_proxy_port, "0"));
        if (hasCommand(m_cmd_proxy_host) && proxyPort > 0) {
            HttpProxyOptions proxyOptions = new HttpProxyOptions();
            proxyOptions.setHost(getCommand(m_cmd_proxy_host));
            proxyOptions.setPort(proxyPort);
            builder.withHttpProxyOptions(proxyOptions);
        }
    }

    /**
     *  build mqtt connection
     */
    public MqttClientConnection buildMqttConnection(MqttClientConnectionEvents callbacks) {
        if (hasCommand(m_cmd_pkcs11_lib)) {
            return buildPkcs11MqttConnection(callbacks);
        } else if (hasCommand(m_cmd_signing_region)) {
            if (hasCommand(m_cmd_x509_endpoint)) {
                return buildWebsocketX509MqttConnection(callbacks);
            } else {
                return buildWebsocketMqttConnection(callbacks);
            }
        } else if (hasCommand(m_cmd_custom_auth_authorizer_name)) {
            return buildDirectMqttConnectionWithCustomAuthorizer(callbacks);
        } else {
            return buildDirectMqttConnection(callbacks);
        }
    }

    /**
     *  sample connection
     */
    public void sampleConnectAndDisconnect(MqttClientConnection connection)
            throws CrtRuntimeException, InterruptedException, ExecutionException {
        try {
            // Connect and disconnect
            CompletableFuture<Boolean> connected = connection.connect();
            try {
                boolean sessionPresent = connected.get();
                System.out.println("Connected to " + (!sessionPresent ? "new" : "existing") + " session!");
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                throw new RuntimeException("Exception occurred during connect", ex);
            }
            System.out.println("Disconnecting...");
            CompletableFuture<Void> disconnected = connection.disconnect();
            disconnected.get();
            System.out.println("Disconnected.");
        } catch (CrtRuntimeException | InterruptedException | ExecutionException ex) {
            throw ex;
        }
    }
}

class CommandLineOption {
    private String commandName;
    private String exampleInput;
    private String helpOutput;

    CommandLineOption(String name, String example, String help) {
        commandName = name;
        exampleInput = example;
        helpOutput = help;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getExampleInput() {
        return exampleInput;
    }

    public void setExampleInput(String exampleInput) {
        this.exampleInput = exampleInput;
    }

    public String getHelpOutput() {
        return helpOutput;
    }

    public void setHelpOutput(String helpOutput) {
        this.helpOutput = helpOutput;
    }
}
