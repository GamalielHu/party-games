package bo.app.partygames.data

/**
 * Frases por tipo de grupo. Cada lista se marca con los grupos donde sirve, así una misma
 * frase se reutiliza sin copiarla (p. ej. las de "Entre amigos" también salen en "Fiesta").
 */
object AudiencePrompts {

    private class Tagged(val audiences: Set<Audience>, val items: List<String>)

    private fun pool(audience: Audience, vararg lists: Tagged): List<String> =
        lists.filter { audience in it.audiences }.flatMap { it.items }.distinct()

    private val ALL_ADULT = setOf(Audience.PARTY, Audience.FRIENDS)

    fun truths(a: Audience) = pool(
        a,
        Tagged(setOf(Audience.FAMILY), Prompts.truths),
        Tagged(setOf(Audience.PARTY), Prompts.spicyTruths),
        Tagged(ALL_ADULT, friendsTruths),
        Tagged(setOf(Audience.COUPLE), coupleTruths),
    )

    fun dares(a: Audience) = pool(
        a,
        Tagged(setOf(Audience.FAMILY), Prompts.dares),
        Tagged(setOf(Audience.PARTY), Prompts.spicyDares),
        Tagged(ALL_ADULT, friendsDares),
        Tagged(setOf(Audience.COUPLE), coupleDares),
    )

    fun neverHaveIEver(a: Audience) = pool(
        a,
        Tagged(setOf(Audience.FAMILY), Prompts.neverHaveIEver),
        Tagged(ALL_ADULT, Prompts.spicyNeverHaveIEver),
        Tagged(ALL_ADULT, friendsNeverHaveIEver),
        Tagged(setOf(Audience.COUPLE), coupleNeverHaveIEver),
    )

    fun mostLikely(a: Audience) = pool(
        a,
        Tagged(setOf(Audience.FAMILY), Prompts.mostLikely),
        Tagged(setOf(Audience.PARTY), Prompts.spicyMostLikely),
        Tagged(ALL_ADULT, friendsMostLikely),
        Tagged(setOf(Audience.COUPLE), coupleMostLikely),
    )

    // ------------------------------------------------------------ Entre amigos / amigas

    private val friendsTruths = listOf(
        "¿Qué chisme del grupo nunca te animaste a contar?",
        "¿Cuál de nuestras exparejas te caía peor y por qué?",
        "¿Quién es tu crush ahora mismo? Nombre y apellido.",
        "¿Qué es lo que más te molesta de alguien de este grupo?",
        "¿Cuál fue la peor decisión que tomaste por un chico o una chica?",
        "¿A quién de tus exes volverías a darle una oportunidad?",
        "¿Qué es lo más tóxico que hiciste en una relación?",
        "¿Cuál es el secreto que le guardas a alguien de aquí?",
        "¿Alguna vez hablaste mal de alguien de este grupo a sus espaldas? ¿Qué dijiste?",
        "¿Qué mensaje borraste antes de que alguien lo viera?",
        "¿Cuál fue tu primer beso y cómo fue?",
        "¿Qué es lo que más te arrepientes de haberle dicho a un ex?",
        "¿Con quién de nuestros conocidos saldrías si nadie se enterara?",
        "¿Cuál es la red flag que siempre ignoras?",
        "¿Qué es lo más loco que hiciste por despecho?",
        "¿Has revisado el celular de tu pareja? ¿Qué encontraste?",
        "¿Cuál es tu mayor inseguridad?",
        "¿Qué amistad terminaste y por qué en realidad?",
        "¿Qué es lo que más envidias de alguien de este grupo?",
        "¿Cuál fue la noche de fiesta de la que menos te acuerdas?",
        "¿Has tenido algo con alguien que a tus amigos no les caía bien?",
        "¿Qué opinas de verdad del último ligue de alguien de aquí?",
        "¿Qué es lo más vergonzoso que publicaste y luego borraste?",
        "¿Cuál es la mentira que más repites?",
        "¿Alguna vez te gustó alguien que le gustaba a tu mejor amigo/a?",
        "¿Qué es lo que nunca le perdonarías a una amiga o amigo?",
        "¿Quién de aquí crees que se casa primero y quién nunca?",
        "¿Qué te dijo tu ex que todavía te duele?",
        "¿Cuál fue el peor beso de tu vida?",
        "¿Cuál es el chisme más fuerte que sabes de alguien conocido?",
    )

    private val friendsDares = listOf(
        "Muestra la última conversación con tu crush o con tu ex.",
        "Deja que el grupo escriba un mensaje a tu crush y mándalo (algo leve, sin mentir).",
        "Imita a tu ex hasta que el grupo se ría.",
        "Lee en voz alta tus últimos tres mensajes enviados.",
        "Deja que alguien del grupo revise tu galería durante 20 segundos.",
        "Haz una historia de Instagram diciendo que alguien de aquí es tu persona favorita.",
        "Llama a alguien de tus contactos y cántale una canción romántica.",
        "Cuenta con detalle tu peor cita.",
        "Deja que el grupo elija tu foto de perfil por 24 horas.",
        "Haz tu mejor perreo durante 20 segundos.",
        "Toma un trago por cada ex que tengas.",
        "Manda un audio a tu mamá o papá diciendo que los quieres mucho, con voz dramática.",
        "Imita cómo habla cada persona del grupo cuando está borracha.",
        "Cuenta un secreto tuyo que nadie de aquí sepa, o toma dos tragos.",
        "Responde con la verdad la próxima pregunta que te haga el grupo, sin pasar.",
        "Deja que te maquillen o peinen como el grupo quiera.",
        "Publica un estado con la frase que elija el grupo (sin ofender a nadie).",
        "Muestra el último video que guardaste.",
        "Di el nombre de la persona con la que más hablas por chat y muéstralo.",
        "Bebe sin manos.",
        "Dale tu celular desbloqueado al de tu derecha por 30 segundos.",
        "Haz una declaración de amor exagerada a tu mejor amigo/a del grupo.",
        "Habla solo con indirectas hasta tu próximo turno.",
        "Muestra tu última búsqueda de Google, o toma dos tragos.",
        "Cuenta cómo conociste a tu último ligue, actuando cada parte.",
    )

    private val friendsNeverHaveIEver = listOf(
        "Yo nunca nunca he leído el chat de una amiga o amigo sin permiso.",
        "Yo nunca nunca he hablado mal de alguien de este grupo.",
        "Yo nunca nunca he tenido algo con el ex de una amiga o amigo.",
        "Yo nunca nunca he usado a un amigo/a como excusa para salir.",
        "Yo nunca nunca he contado un secreto que me pidieron guardar.",
        "Yo nunca nunca he cancelado planes para quedarme con alguien que me gustaba.",
        "Yo nunca nunca he stalkeado a alguien hasta llegar a sus fotos de hace años.",
        "Yo nunca nunca le he dado like por error a una foto vieja.",
        "Yo nunca nunca he tenido un perfil falso.",
        "Yo nunca nunca he llorado por alguien que no lo merecía.",
        "Yo nunca nunca he vuelto a escribirle a alguien que me dejó en visto.",
        "Yo nunca nunca he mentido sobre dónde estaba.",
        "Yo nunca nunca he tomado tanto que tuvieron que llevarme a casa.",
        "Yo nunca nunca he tenido un crush con alguien comprometido.",
        "Yo nunca nunca he contestado \"estoy bien\" sin estarlo para no dar explicaciones.",
        "Yo nunca nunca he mandado captura de un chat al grupo de amigos.",
        "Yo nunca nunca he fingido no ver a alguien en la calle.",
        "Yo nunca nunca he tenido envidia de una amiga o amigo.",
        "Yo nunca nunca he salido con alguien solo porque estaba aburrido/a.",
        "Yo nunca nunca he borrado fotos con un ex.",
    )

    private val friendsMostLikely = listOf(
        "¿Quién es más probable que cuente el chisme antes de que termine la noche?",
        "¿Quién es más probable que vuelva con alguien que todos odiamos?",
        "¿Quién es más probable que stalkee a su crush ahora mismo?",
        "¿Quién es más probable que se enamore del primero que le hable bonito?",
        "¿Quién es más probable que tenga un chat secreto?",
        "¿Quién es más probable que ignore todas las red flags?",
        "¿Quién es más probable que deje al grupo plantado por una cita?",
        "¿Quién es más probable que llore en la fiesta por un ex?",
        "¿Quién es más probable que tenga el ex más tóxico?",
        "¿Quién es más probable que mande indirectas en sus historias?",
        "¿Quién es más probable que sea el confidente de todos?",
        "¿Quién es más probable que se haga amigo/a del ex de alguien del grupo?",
        "¿Quién es más probable que responda \"jaja\" sin reírse?",
        "¿Quién es más probable que esté hablando con alguien y no lo haya contado?",
        "¿Quién es más probable que organice la próxima salida y la cancele?",
        "¿Quién es más probable que tenga capturas de todo el mundo?",
        "¿Quién es más probable que se case primero?",
        "¿Quién es más probable que se quede soltero/a por elegir mal siempre?",
        "¿Quién es más probable que se pelee por defender a una amiga o amigo?",
        "¿Quién es más probable que tenga más chismes de los demás?",
    )

    // ------------------------------------------------------------ Pareja

    private val coupleTruths = listOf(
        "¿Qué fue lo primero que te gustó de mí?",
        "¿Cuándo supiste que te gustaba de verdad?",
        "¿Qué es lo que más te prende de mí?",
        "¿Cuál ha sido nuestro mejor beso y dónde fue?",
        "¿Qué fantasía te gustaría cumplir conmigo?",
        "¿Qué es lo que nunca te animaste a pedirme?",
        "¿Alguna vez sentiste celos de alguien? ¿De quién?",
        "¿Qué parte de mi cuerpo es tu favorita?",
        "¿Qué cosa hago que te molesta y nunca me dijiste?",
        "¿Cuál es tu recuerdo favorito de nosotros?",
        "¿Qué te gustaría que hiciera más seguido?",
        "¿En qué lugar te gustaría que tuviéramos una noche especial?",
        "¿Qué ropa mía te gusta más?",
        "Del 1 al 10, ¿qué tan romántico/a soy? ¿Por qué?",
        "¿Alguna vez pensaste en terminar? ¿Por qué?",
        "¿Qué es lo más atrevido que te gustaría que hiciéramos?",
        "¿Qué canción te recuerda a mí?",
        "¿Qué es lo que más te costó contarme?",
        "¿Te has fijado en alguien más estando conmigo?",
        "¿Cuál es tu momento favorito del día conmigo?",
        "¿Qué te gustaría cambiar de nuestra relación?",
        "¿Qué te dije alguna vez que nunca olvidaste?",
        "¿Cómo imaginas nuestra vida en cinco años?",
        "¿Cuál fue la vez que más te enojaste conmigo?",
        "¿Qué es lo más sexy que hago sin darme cuenta?",
        "¿Qué pensaste la primera vez que me viste?",
        "¿Con qué disfraz te gustaría verme?",
        "¿Cuál es tu forma favorita de que te despierte?",
        "¿Qué secreto tuyo todavía no conozco?",
        "¿Qué es lo que más te gusta que te diga?",
    )

    private val coupleDares = listOf(
        "Dame un beso de 10 segundos.",
        "Hazme un masaje de un minuto donde yo elija.",
        "Susúrrame al oído lo que te gustaría hacer esta noche.",
        "Bésame en un lugar que no sea la boca.",
        "Baila sensual para mí durante 30 segundos.",
        "Quítate una prenda que yo elija.",
        "Mírame a los ojos durante un minuto sin reírte.",
        "Dime tres cosas que te encantan de mí, sin repetir ninguna que ya hayas dicho antes.",
        "Imita cómo te coqueteé la primera vez.",
        "Deja que te venda los ojos y adivina con un beso dónde te toqué.",
        "Mándame el mensaje más atrevido que se te ocurra, aunque estemos juntos.",
        "Recrea nuestro primer beso.",
        "Dame un abrazo de un minuto entero.",
        "Elige una canción y bailemos pegados.",
        "Hazme un cumplido en otro idioma.",
        "Muérdeme suavemente la oreja.",
        "Escríbeme algo con el dedo en la espalda y que yo adivine qué es.",
        "Dame de comer o de tomar algo sin usar las manos.",
        "Cuéntame tu fantasía en tres palabras.",
        "Deja que te dé un beso donde yo quiera.",
        "Durante los próximos tres turnos, siéntate pegado/a a mí.",
        "Haz una pose sexy y deja que te tome una foto (solo para mí).",
        "Cántame una canción romántica mirándome a los ojos.",
        "Dime cómo sería nuestra cita perfecta, con todos los detalles.",
        "Dame un beso en el cuello.",
        "Cambia de prenda conmigo hasta el final del juego.",
        "Hazme cosquillas hasta que me rinda o hasta 20 segundos.",
        "Elige un castigo para ti mismo/a y cúmplelo.",
        "Hazme un masaje en los pies durante un minuto.",
        "Ponte en modo seductor y convénceme de algo que yo elija.",
    )

    private val coupleNeverHaveIEver = listOf(
        "Yo nunca nunca he revisado tu celular a escondidas.",
        "Yo nunca nunca he sentido celos de alguien de tu trabajo o clase.",
        "Yo nunca nunca he fingido que me gustaba un regalo tuyo.",
        "Yo nunca nunca he pensado en ti en un momento poco apropiado.",
        "Yo nunca nunca he soñado contigo algo atrevido.",
        "Yo nunca nunca he stalkeado a tus exes.",
        "Yo nunca nunca te he mentido sobre dónde estaba.",
        "Yo nunca nunca he guardado una foto tuya que no sabes que tengo.",
        "Yo nunca nunca he querido besarte en un lugar público y me aguanté.",
        "Yo nunca nunca he fingido estar dormido/a para no hablar.",
        "Yo nunca nunca he usado tu ropa cuando no estabas.",
        "Yo nunca nunca he contado algo nuestro a mis amigos/as que tú no sabes.",
        "Yo nunca nunca he pensado en casarme contigo.",
        "Yo nunca nunca he tenido una fantasía que no te he contado.",
        "Yo nunca nunca me he enojado contigo sin que te enteres por qué.",
        "Yo nunca nunca he planeado una sorpresa que no resultó.",
        "Yo nunca nunca he extrañado a un ex estando contigo.",
        "Yo nunca nunca he buscado en internet ideas para sorprenderte en la intimidad.",
        "Yo nunca nunca he leído nuestros chats viejos.",
        "Yo nunca nunca he querido que me llames con un apodo diferente.",
    )

    private val coupleMostLikely = listOf(
        "¿Quién de los dos es más probable que dé el primer beso esta noche?",
        "¿Quién de los dos es más probable que se ponga celoso/a primero?",
        "¿Quién de los dos es más probable que pida perdón después de una pelea?",
        "¿Quién de los dos es más probable que olvide un aniversario?",
        "¿Quién de los dos es más probable que proponga algo atrevido?",
        "¿Quién de los dos es más probable que se duerma primero?",
        "¿Quién de los dos es más probable que planee la próxima cita?",
        "¿Quién de los dos es más probable que diga \"te amo\" primero hoy?",
        "¿Quién de los dos es más probable que revise el celular del otro?",
        "¿Quién de los dos es más probable que gaste más en un regalo?",
        "¿Quién de los dos es más probable que llore en una película?",
        "¿Quién de los dos es más probable que sea más romántico/a?",
        "¿Quién de los dos es más probable que tome la iniciativa en la cama?",
        "¿Quién de los dos es más probable que se enoje por hambre?",
        "¿Quién de los dos es más probable que robe la cobija?",
        "¿Quién de los dos es más probable que quiera tener hijos primero?",
        "¿Quién de los dos es más probable que haga una escena de celos en público?",
        "¿Quién de los dos es más probable que mande un mensaje atrevido en horario de trabajo?",
        "¿Quién de los dos es más probable que se rinda primero en una discusión?",
        "¿Quién de los dos es más probable que organice una noche sorpresa?",
    )
}
