
from subprocess import run as run_shell
from xml.etree import ElementTree
from sys import stderr


def run(cmd):
    return run_shell(cmd, shell=True, check=True, universal_newlines=True, capture_output=True).stdout.strip()


def extract_version(xml):
    xml = ElementTree.fromstring(xml)
    versions = tuple(elem.text for elem in xml if elem.tag.endswith('version'))
    assert len(versions) == 1
    return versions[0]


def split_version(version_txt):
    return tuple(int(digit) for digit in version_txt.split('.'))


def fmt_version(version):
    return ".".join(str(v) for v in version)


def main():
    base_commit = run('git merge-base HEAD origin/main')
    base_pom = run(f'git show {base_commit}:pom.xml')
    head_pom = run(f'git show HEAD:pom.xml')

    base_version = extract_version(base_pom)
    head_version = extract_version(head_pom)

    if base_version >= head_version:
        stderr.write(f'version should be bumped, currently {head_version}, while base is {base_version}\n')

    version_parts = split_version(base_version)
    next_version = fmt_version((version_parts[0], version_parts[1] + 1, version_parts[2]))

    new_pom = head_pom.replace(f'<version>{base_version}</version>', f'<version>{next_version}</version>', 1)
    with open('pom.xml', 'w') as fh:
        fh.write(new_pom)


if __name__ == '__main__':
    main()

