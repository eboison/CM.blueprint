<#-- @ftlvariable name="self" type="com.coremedia.livecontext.contentbeans.LiveContextExternalChannel" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />
<#import "../../freemarkerLibs/heroTeaser.ftl" as heroTeaser />

<@utils.optionalLink href=heroTeaser.getLink(self.target!cm.UNDEFINED, self.teaserSettings)
                     openInNewTab=self.openInNewTab>
  <@cm.include self=self view="_picture" params={
    "blockClass": cm.localParameters().heroBlockClass!"cm-hero",
    "renderEmptyImage": cm.localParameters().renderEmptyImage!true
  }/>
</@utils.optionalLink>