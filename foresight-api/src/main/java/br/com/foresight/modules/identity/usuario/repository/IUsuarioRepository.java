package br.com.foresight.modules.identity.usuario.repository;

import br.com.foresight.modules.identity.usuario.entity.Usuario;
import br.com.foresight.modules.identity.usuario.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // === SOLUÇÃO DO ERRO AQUI ===
    // O Spring Data cria automaticamente a query: SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE u.role = ?1
    boolean existsByRole(Role role);
}