/**
 * This package contains HTML help pages loaded by
 * {@link cz.cuni.lf1.lge.ThunderSTORM.UI.HelpButton}. The html pages are
 * converted automatically from lyx files.
 *
 * The conversion takes multiple steps:
 * <ul>
 * <li>lyx -> plain latex. Lyx is required for this step. The lyx executable has
 * to be in path or {@code lyx.path} property has to be set in
 * {@code build.properties}</li>
 * <li>tex -> xml. This step is done using latexml. You need working tex
 * distribution with all the packages used in the lyx documents and a working
 * installation of latexml. Currently latexml has broblems building on windows
 * so a linux machine might be needed. Latexml supports only a subset of latexml
 * packages so some lyx features cannot be converted to html correctly.</li>
 * <li>xml -> html. Using latexmlpost, a part of latexml.</li>
 * </ul>
 *
 * There is an ant target for the conversion (compile-html-help) and there is
 * another ant target (clean html help) to delete intermediate files created
 * during the conversion.
 * <p/>
 * The file {@code customRules.css} is linked to all files during coversion.
 * Additional rules can be added to this file to customize appearance.
 *
 */
package resources.help;
