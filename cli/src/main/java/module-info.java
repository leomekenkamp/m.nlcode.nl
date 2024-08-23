module nl.nlcode.m.cli {
    requires nl.nlcode.m.engine;
    requires org.slf4j;
    requires info.picocli;
    requires org.jline;

    opens nl.nlcode.m.cli to info.picocli;
}