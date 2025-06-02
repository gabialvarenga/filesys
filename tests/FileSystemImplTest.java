package tests;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.FileSystemImpl;
import model.Usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

class FileSystemImplTest {
    private FileSystemImpl fs;

    @BeforeEach
    void setUp() {
        fs = new FileSystemImpl(Collections.singletonList(new Usuario("root", "rwx", "/")));
    }

    @Test
    void mkdir_CriaDiretorioComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        assertDoesNotThrow(() -> fs.mkdir("/docs2", "root"));
    }

    @Test
    void mkdir_LancaExcecaoSeDiretorioJaExiste() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(CaminhoJaExistenteException.class, () -> fs.mkdir("/docs", "root"));
    }

    @Test
    void mkdir_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(PermissaoException.class, () -> fs.mkdir("/docs/privado", "usuario"));
    }

    @Test
    void touch_CriaArquivoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        assertThrows(CaminhoJaExistenteException.class, () -> fs.touch("/docs/arquivo.txt", "root"));
    }

    @Test
    void touch_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(PermissaoException.class, () -> fs.touch("/docs/novo.txt", "usuario"));
    }

    @Test
    void chmod_AlteraPermissaoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        assertDoesNotThrow(() -> fs.chmod("/docs", "root", "usuario", "rwx"));
    }

    @Test
    void chmod_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(PermissaoException.class, () -> fs.chmod("/docs", "usuario", "usuario", "rwx"));
    }

    @Test
    void chmod_LancaExcecaoSeCaminhoNaoExiste() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.chmod("/naoexiste", "root", "root", "rwx"));
    }

    @Test
    void rm_RemoveArquivoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        assertDoesNotThrow(() -> fs.rm("/docs/arquivo.txt", "root"));
    }

    @Test
    void rm_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        assertThrows(PermissaoException.class, () -> fs.rm("/docs/arquivo.txt", "usuario", false));
    }

    @Test
    void rm_LancaExcecaoSeCaminhoNaoExiste() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.rm("/naoexiste.txt", "root", false));
    }

    @Test
    void write_EscreveEmArquivoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        assertDoesNotThrow(() -> fs.write("/docs/arquivo.txt", "root", false, "conteudo".getBytes()));
    }

    @Test
    void write_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        assertThrows(PermissaoException.class, () -> fs.write("/docs/arquivo.txt", "usuario", false, "conteudo".getBytes()));
    }

   @Test
    void read_LerArquivoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        fs.write("/docs/arquivo.txt", "root", false, "conteudo".getBytes());
        byte[] buffer = new byte[100];
        fs.read("/docs/arquivo.txt", "root", buffer);
        String lido = new String(buffer).trim();
        assertTrue(lido.startsWith("conteudo"));
    }

    @Test
    void read_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        fs.write("/docs/arquivo.txt", "root", false, "conteudo".getBytes());
        byte[] buffer = new byte[100];
        assertThrows(PermissaoException.class, () -> fs.read("/docs/arquivo.txt", "usuario", buffer));
    }

    @Test
    void mv_MoveArquivoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        fs.mkdir("/destino", "root");
        assertDoesNotThrow(() -> fs.mv("/docs/arquivo.txt", "/destino/arquivo.txt", "root"));
    }

    @Test
    void mv_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        fs.mkdir("/destino", "root");
        assertThrows(PermissaoException.class, () -> fs.mv("/docs/arquivo.txt", "/destino/arquivo.txt", "usuario"));
    }

    @Test
    void ls_ListaConteudoDiretorioComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        String[] conteudo = fs.ls("/docs", "root");
        assertArrayEquals(new String[]{"arquivo.txt"}, conteudo);
    }

    @Test
    void ls_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        assertThrows(PermissaoException.class, () -> fs.ls("/docs", "usuario", false));
    }

    @Test
    void cp_CopiaArquivoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        fs.write("/docs/arquivo.txt", "root",false, "conteudo".getBytes());
        fs.mkdir("/destino", "root");
        assertDoesNotThrow(() -> fs.cp("/docs/arquivo.txt", "/destino/arquivo.txt", "root",false));
        byte[] buffer = new byte["conteudo".length()]; // Use the length of the expected content
        fs.read("/destino/arquivo.txt", "root", buffer);
        String conteudo = new String(buffer).trim();
        assertEquals("conteudo", conteudo);

    }

    @Test
    void cp_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        fs.mkdir("/destino", "root");
        assertThrows(PermissaoException.class, () -> fs.cp("/docs/arquivo.txt", "/destino/arquivo.txt", "usuario",false));
    }
}
